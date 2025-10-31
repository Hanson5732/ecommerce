package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.OrderItem;
import java.util.Optional;
import java.util.UUID;

// OrderItem 数据访问对象接口
public interface OrderItemDao {

    /**
     * 根据主键 ID 查找订单项
     */
    Optional<OrderItem> findById(String itemId);

    /**
     * 更新一个订单项 (主要用于更新状态)
     */
    OrderItem update(OrderItem item);

    /**
     * 统计指定用户的未读订单项数量
     */
    long countUnreadByUserId(UUID userId);

    /**
     * 将指定用户的所有未读订单项标记为已读
     * @return 更新的记录数
     */
    int markAllAsReadByUserId(UUID userId);

    /**
     * 统计指定用户和特定状态的订单项数量
     * @param userId
     * @param itemStatus
     * @return
     */
    long countByUserIdAndItemStatus(UUID userId, String itemStatus);
}