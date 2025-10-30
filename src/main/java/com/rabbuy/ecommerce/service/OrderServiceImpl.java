package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dao.*; // 导入所有 DAO
import com.rabbuy.ecommerce.dto.*; // 导入所有 DTO
import com.rabbuy.ecommerce.entity.*; // 导入所有 实体
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional; // 关键：用于事务
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    @Inject private OrderDao orderDao;
    @Inject private OrderItemDao orderItemDao;
    @Inject private ProductDao productDao;
    @Inject private UserDao userDao;
    @Inject private AddressDao addressDao;
    @Inject private CommentDao commentDao;

    // --- 创建订单 (核心事务) ---
    @Override
    @Transactional
    public OrderCreatedDto createOrder(OrderCreateDto createDto) throws NotFoundException, IllegalStateException {
        //
        if (createDto.products() == null || createDto.products().isEmpty()) {
            throw new IllegalArgumentException("Item list is empty");
        }

        // 1. 获取关联实体
        User user = userDao.findById(createDto.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Address address = addressDao.findById(createDto.addressId())
                .orElseThrow(() -> new NotFoundException("Address not found"));

        // 2. 创建 Order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setDeliveryTime(createDto.deliveryTime() != null ? createDto.deliveryTime() : "0");
        order.setOrderStatus("0"); // 默认未支付

        // 3. (在事务中) 处理订单项和库存
        for (int i = 0; i < createDto.products().size(); i++) {
            CartItem itemDto = createDto.products().get(i);
            UUID productId = itemDto.getId();
            int count = itemDto.getCount();

            Product product = productDao.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

            // 检查库存
            if (product.getStockQuantity() < count) {
                throw new IllegalStateException(product.getProductName() + " is understock");
            }

            // 减少库存
            product.setStockQuantity(product.getStockQuantity() - count);
            productDao.update(product); // 持久化库存变更

            // 创建商品快照
            ProductSnapshot snapshot = new ProductSnapshot(
                    product.getProductId(),
                    product.getProductName(),
                    product.getPrice(), // 存储下单时的价格
                    (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null,
                    count
            );

            // 创建 OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(order.getOrderId().toString() + "-" + i); // 手动生成 ID
            orderItem.setProduct(snapshot);
            orderItem.setItemStatus("0"); // 默认未支付

            order.addItem(orderItem); // 添加到 Order 的集合中
        }

        // 4. 保存 Order (由于 CascadeType.ALL, OrderItems 会被一并保存)
        orderDao.save(order);

        // 5. 返回 DTO
        return new OrderCreatedDto(
                order.getOrderId(),
                order.getDeliveryTime(),
                order.getUser().getId(),
                order.getAddress().getAddressId(),
                order.getOrderStatus()
        );
    }

    // --- 获取订单详情 ---
    @Override
    public OrderDetailResponseDto getOrderDetails(UUID orderId) throws NotFoundException {
        //
        Order order = orderDao.findOrderWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemResponseDto> itemDtos = new ArrayList<>();
        OffsetDateTime createdTime = null; // (取第一个 item 的创建时间)

        for (OrderItem item : order.getItems()) {
            ProductSnapshot snapshot = item.getProduct();

            // 计算总价
            if (snapshot.getPrice() != null) {
                totalAmount = totalAmount.add(snapshot.getPrice().multiply(new BigDecimal(snapshot.getCount())));
            }

            // 设置创建时间
            if (createdTime == null) {
                createdTime = item.getCreatedTime();
            }

            itemDtos.add(new OrderItemResponseDto(
                    snapshot.getId(),
                    snapshot.getName(),
                    snapshot.getPrice(),
                    snapshot.getImage(),
                    snapshot.getCount(),
                    item.getItemId(),
                    item.getItemStatus(),
                    item.getUpdatedTime()
            ));
        }

        return new OrderDetailResponseDto(
                order.getOrderId(),
                order.getDeliveryTime(),
                itemDtos,
                order.getOrderStatus(),
                totalAmount,
                createdTime
        );
    }

    // --- 更新订单状态 ---
    @Override
    @Transactional
    public OrderDetailResponseDto updateOrder(OrderUpdateDto updateDto) throws NotFoundException {
        //
        Order order = orderDao.findOrderWithItems(updateDto.orderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (updateDto.deliveryTime() != null) {
            order.setDeliveryTime(updateDto.deliveryTime());
        }
        if (updateDto.address() != null) {
            Address address = addressDao.findById(updateDto.address())
                    .orElseThrow(() -> new NotFoundException("Address not found"));
            order.setAddress(address);
        }
        if (updateDto.orderStatus() != null) {
            order.setOrderStatus(updateDto.orderStatus());

            // 级联更新 OrderItems 状态
            String newItemStatus = updateDto.orderStatus();
            if ("1".equals(newItemStatus) || "2".equals(newItemStatus)) {
                for (OrderItem item : order.getItems()) {
                    item.setItemStatus(newItemStatus);
                    // item.setUpdatedTime(OffsetDateTime.now()); // PreUpdate 会自动处理
                }
            }
        }

        Order updatedOrder = orderDao.update(order); // Merge 更改

        // 返回更新后的详情
        return getOrderDetails(updatedOrder.getOrderId());
    }

    // --- 客户更新订单项状态 ---
    @Override
    @Transactional
    public OrderItem updateCustomerItemStatus(OrderItemStatusUpdateDto updateDto) throws NotFoundException, IllegalArgumentException {
        //
        // 1. 验证状态转换
        Map<String, List<String>> validTransitions = Map.of(
                "1", List.of("6"), // 待发货 -> 申请退款
                "9", List.of("6"), // 待付款 -> 申请退款 (原文如此)
                "3", List.of("6"), // 待收货 -> 申请退款
                "4", List.of("5", "6"), // 已发货 -> 确认收货 / 申请退款
                "5", List.of("6")  // 已收货 -> 申请退款
        );

        if (!validTransitions.containsKey(updateDto.oldStatus()) ||
                !validTransitions.get(updateDto.oldStatus()).contains(updateDto.newStatus())) {
            throw new IllegalArgumentException("Invalid status transformation");
        }

        // 2. 获取并更新
        OrderItem item = orderItemDao.findById(updateDto.itemId())
                .orElseThrow(() -> new NotFoundException("Order item not found"));

        if (!item.getItemStatus().equals(updateDto.oldStatus())) {
            throw new IllegalArgumentException("Item status does not match oldStatus");
        }

        item.setItemStatus(updateDto.newStatus());
        // PreUpdate 会自动设置 updated_time

        return orderItemDao.update(item); // 返回更新后的实体
    }

    // --- 获取客户订单列表（复杂） ---
    @Override
    public PaginatedResult<OrderListDto> getOrdersByUserId(UUID userId, String itemStatus, int page, int pageSize) {
        //

        // 1. 获取相关 Order IDs
        List<UUID> orderIds;
        if (itemStatus != null && !itemStatus.isEmpty() && !"all".equals(itemStatus) && !"undefined".equals(itemStatus)) {
            orderIds = orderDao.findOrderIdsByUserIdAndItemStatus(userId, itemStatus);
        } else {
            orderIds = orderDao.findOrderIdsByUserId(userId);
        }

        if (orderIds.isEmpty()) {
            return new PaginatedResult<>(new ArrayList<>(), 0, page, 0);
        }

        // 2. 批量获取所有相关的 Orders 及其 Items
        List<Order> orders = orderDao.findOrdersWithItemsByOrderIds(orderIds);

        // 3. 在内存中分组、排序
        Map<UUID, OrderListDto> orderMap = new HashMap<>();

        for (Order order : orders) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            OffsetDateTime latestTime = null;
            int maxStatus = -1;
            List<OrderItemListDto> itemDtos = new ArrayList<>();

            for (OrderItem item : order.getItems()) {
                ProductSnapshot snapshot = item.getProduct();
                if (snapshot == null) continue; // 安全检查

                // 计算总价
                if (snapshot.getPrice() != null) {
                    totalAmount = totalAmount.add(snapshot.getPrice().multiply(new BigDecimal(snapshot.getCount())));
                }

                // 查找最新时间和最高状态
                if (latestTime == null || item.getCreatedTime().isAfter(latestTime)) {
                    latestTime = item.getCreatedTime();
                }
                try {
                    int statusNum = Integer.parseInt(item.getItemStatus());
                    if (statusNum > maxStatus) {
                        maxStatus = statusNum;
                    }
                } catch (NumberFormatException e) { /* 忽略无效状态 */ }

                // 转换 Item DTO
                itemDtos.add(new OrderItemListDto(
                        item.getItemId(),
                        snapshot.getId(),
                        item.getItemStatus(),
                        snapshot.getName(),
                        snapshot.getImage(),
                        item.getCreatedTime(),
                        item.getUpdatedTime(),
                        snapshot.getPrice(),
                        snapshot.getCount()
                ));
            }

            if (latestTime == null && !order.getItems().isEmpty()) {
                latestTime = order.getItems().get(0).getCreatedTime(); // 备用
            }

            OrderListDto orderListDto = new OrderListDto(
                    order.getOrderId().toString(),
                    latestTime,
                    String.valueOf(maxStatus),
                    totalAmount,
                    0, // PostFee (原文写死 0)
                    itemDtos
            );
            orderMap.put(order.getOrderId(), orderListDto);
        }

        // 4. 排序 (按最新时间降序)
        List<OrderListDto> sortedOrders = orderMap.values().stream()
                .sorted(Comparator.comparing(OrderListDto::createdTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // 5. 内存中分页
        int totalItems = sortedOrders.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, totalItems);

        List<OrderListDto> paginatedData = (start < totalItems) ? sortedOrders.subList(start, end) : new ArrayList<>();

        return new PaginatedResult<>(paginatedData, totalItems, page, totalPages);
    }

    // --- 其他方法 ---

    @Override
    public OrderItemCommentStatusDto getOrderItemForComment(String itemId) throws NotFoundException {
        //
        OrderItem item = orderItemDao.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Order item not found"));

        boolean isCommented = commentDao.existsByOrderItemId(itemId);

        return new OrderItemCommentStatusDto(
                item.getItemId(),
                item.getProduct(),
                isCommented
        );
    }

    @Override
    public OrderNotificationCountDto getNotificationCount(UUID userId) {
        //
        long count = orderItemDao.countUnreadByUserId(userId);
        return new OrderNotificationCountDto(count);
    }

    @Override
    @Transactional
    public void markNotificationsAsRead(UUID userId) {
        //
        orderItemDao.markAllAsReadByUserId(userId);
    }
}