package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.entity.Order;
import java.util.List;
import java.util.Optional;

// Order 数据访问对象接口
public interface OrderDao {

    /**
     * 保存一个新订单及其所有订单项 (需要 CascadeType.ALL)
     */
    void save(Order order);

    /**
     * 更新一个现有订单
     */
    Order update(Order order);

    /**
     * 根据 ID 查找订单，并立即加载其所有订单项
     */
    Optional<Order> findOrderWithItems(String orderId);

    /**
     * (辅助 get_order_by_user_id_view) 查找用户的所有 Order ID
     */
    List<String> findOrderIdsByUserId(String userId);

    /**
     * (辅助 get_order_by_user_id_view) 查找用户 ID 和特定 Item 状态的 Order ID
     */
    List<String> findOrderIdsByUserIdAndItemStatus(String userId, String itemStatus);

    /**
     * (辅助 get_order_by_user_id_view) 根据 Order ID 列表查找所有 Orders 及其 Items
     */
    List<Order> findOrdersWithItemsByOrderIds(List<String> orderIds);

    /**
     * (辅助 get_all_orders_view) 分页查找所有订单（管理员用）
     */
    PaginatedResult<Order> findAdminOrders(String query, int page, int pageSize);

    /**
     * 按订单项状态查找所有订单 ID (管理员)
     *
     * @param itemStatus
     * @return
     */
    List<String> findOrderIdsByItemStatus(String itemStatus);

    /**
     * 查找所有订单 ID (管理员)
     *
     * @return
     */
    List<String> findAllOrderIds();
}