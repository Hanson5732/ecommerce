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
        if (createDto.getProducts() == null || createDto.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Item list is empty");
        }

        // 1. 获取关联实体
        User user = userDao.findById(createDto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Address address = addressDao.findById(createDto.getAddressId())
                .orElseThrow(() -> new NotFoundException("Address not found"));

        // 2. 创建 Order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setDeliveryTime(createDto.getDeliveryTime() != null ? createDto.getDeliveryTime() : "0");
        order.setOrderStatus("0"); // 默认未支付

        // --- 修复点 1：先保存 Order 以生成 ID ---
        // 此时 Hibernate 会执行 insert 并生成 UUID，order.getOrderId() 将不再是 null
        orderDao.save(order);

        // 3. (在事务中) 处理订单项和库存
        for (int i = 0; i < createDto.getProducts().size(); i++) {
            CartItem itemDto = createDto.getProducts().get(i);
            String productId = itemDto.getId();
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
                    product.getPrice(),
                    (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null,
                    count
            );

            // 创建 OrderItem
            OrderItem orderItem = new OrderItem();
            // --- 修复点 2：现在可以使用 orderId 了 ---
            orderItem.setItemId(order.getOrderId() + "-" + i);
            orderItem.setProduct(snapshot);
            orderItem.setItemStatus("0");

            order.addItem(orderItem); // 添加到 Order 的集合中
        }

        // 4. 更新 Order (保存级联的 OrderItems)
        // 因为 Order 已经是 Managed 状态，且方法有 @Transactional，其实不需要显式调用 update 也会提交。
        // 但为了代码清晰，我们可以显式更新。
        orderDao.update(order);

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
    public OrderDetailResponseDto getOrderDetails(String orderId) throws NotFoundException {
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

        AddressDto addressDto = null;
        if (order.getAddress() != null) {
            addressDto = new AddressDto(
                    order.getAddress().getAddressId(),
                    order.getAddress().getAddressTag(),
                    order.getAddress().getRecipientName(),
                    order.getAddress().getPhone(),
                    order.getAddress().getProvince(),
                    order.getAddress().getCity(),
                    order.getAddress().getDistrict(),
                    order.getAddress().getAdditionalAddress(),
                    order.getAddress().getPostalCode(),
                    order.getAddress().isDefault()
            );
        }

        return new OrderDetailResponseDto(
                order.getOrderId(),
                order.getDeliveryTime(),
                itemDtos,
                order.getOrderStatus(),
                totalAmount,
                createdTime,
                order.getUser().getId(),
                order.getUser().getUsername(),
                addressDto
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
    public PaginatedResult<OrderListDto> getOrdersByUserId(String userId, String itemStatus, int page, int pageSize) {
        //

        // 1. 获取相关 Order IDs
        List<String> orderIds;
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
        Map<String, OrderListDto> orderMap = new HashMap<>();

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
    public OrderNotificationCountDto getNotificationCount(String userId) {
        //
        long count = orderItemDao.countUnreadByUserId(userId);
        return new OrderNotificationCountDto(count);
    }

    @Override
    @Transactional
    public void markNotificationsAsRead(String userId) {
        //
        orderItemDao.markAllAsReadByUserId(userId);
    }

    @Override
    public HomeMessageCountDto getMessageCountsByUserId(String userId) {
        long unpaid = orderItemDao.countByUserIdAndItemStatus(userId, "0");
        long pending = orderItemDao.countByUserIdAndItemStatus(userId, "1");
        long review = orderItemDao.countByUserIdAndItemStatus(userId, "5");
        long refunding = orderItemDao.countByUserIdAndItemStatus(userId, "6");

        return new HomeMessageCountDto(unpaid, pending, review, refunding);
    }

    /**
     * 管理员：获取所有订单列表（分页）
     * @param itemStatus
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PaginatedResult<OrderListDto> getAdminOrdersList(String itemStatus, int page, int pageSize) {
        // Django 的 `get_all_orders_view` 与 `get_orders_view`
        // 唯一的区别是它查询所有用户。

        // 1. 获取相关 Order IDs (不限制 userId)
        List<String> orderIds;
        if (itemStatus != null && !itemStatus.isEmpty() && !"all".equals(itemStatus) && !"undefined".equals(itemStatus)) {
            orderIds = orderDao.findOrderIdsByItemStatus(itemStatus);
        } else {
            orderIds = orderDao.findAllOrderIds();
        }

        if (orderIds.isEmpty()) {
            return new PaginatedResult<>(new ArrayList<>(), 0, page, 0);
        }

        // 2. 批量获取所有相关的 Orders 及其 Items
        List<Order> orders = orderDao.findOrdersWithItemsByOrderIds(orderIds);

        // 3. 在内存中分组、排序 (与 getOrdersByUserId 的逻辑相同)
        Map<String, OrderListDto> orderMap = new HashMap<>();

        for (Order order : orders) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            OffsetDateTime latestTime = null;
            int maxStatus = -1;
            List<OrderItemListDto> itemDtos = new ArrayList<>();

            for (OrderItem item : order.getItems()) {
                ProductSnapshot snapshot = item.getProduct();
                if (snapshot == null) continue;

                if (snapshot.getPrice() != null) {
                    totalAmount = totalAmount.add(snapshot.getPrice().multiply(new BigDecimal(snapshot.getCount())));
                }
                if (latestTime == null || item.getCreatedTime().isAfter(latestTime)) {
                    latestTime = item.getCreatedTime();
                }
                try {
                    int statusNum = Integer.parseInt(item.getItemStatus());
                    if (statusNum > maxStatus) {
                        maxStatus = statusNum;
                    }
                } catch (NumberFormatException e) { /* 忽略 */ }

                itemDtos.add(new OrderItemListDto(
                        item.getItemId(), snapshot.getId(), item.getItemStatus(),
                        snapshot.getName(), snapshot.getImage(),
                        item.getCreatedTime(), item.getUpdatedTime(),
                        snapshot.getPrice(), snapshot.getCount()
                ));
            }
            if (latestTime == null && !order.getItems().isEmpty()) {
                latestTime = order.getItems().get(0).getCreatedTime();
            }

            OrderListDto orderListDto = new OrderListDto(
                    order.getOrderId().toString(), latestTime,
                    String.valueOf(maxStatus), totalAmount, 0, itemDtos
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

    /**
     * 【新】管理员：获取订单详情
     * 对应 Django: get_order_detail_view
     *
     */
    @Override
    public OrderDetailResponseDto getAdminOrderDetail(String orderId) throws NotFoundException {
        // Django 的管理员视图和客户视图
        // (get_order_detail_view vs get_order_view)
        // 使用相同的逻辑和序列化器。
        return getOrderDetails(orderId);
    }

    /**
     * 【新】管理员：更新订单项状态
     * 对应 Django: update_admin_item_status_view
     *
     */
    @Override
    @Transactional
    public OrderItem updateAdminItemStatus(AdminOrderItemStatusUpdateDto updateDto) throws NotFoundException, IllegalArgumentException {
        // 1. 获取订单项
        OrderItem item = orderItemDao.findById(updateDto.itemId())
                .orElseThrow(() -> new NotFoundException("Order item not found"));

        String newStatus = updateDto.status();

        // 2. 验证状态
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("New status is required");
        }

        // 3. 业务逻辑 (退款时恢复库存)
        //
        if (("7".equals(newStatus) || "10".equals(newStatus)) && "6".equals(item.getItemStatus())) {
            // "7" = 已退款, "10" = 已拒绝, "6" = 退款中
            Product product = productDao.findById(item.getProduct().getId())
                    .orElse(null); // 产品可能已被删除

            if (product != null) {
                // 退款成功 (7) 或 拒绝退款 (10)，恢复库存
                int count = item.getProduct().getCount();
                product.setStockQuantity(product.getStockQuantity() + count);
                productDao.update(product);
            }
        }

        // 4. 更新状态
        item.setItemStatus(newStatus);

        // 5. 持久化 (PreUpdate 会自动更新时间)
        return orderItemDao.update(item);
    }
}