package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.*;
import com.rabbuy.ecommerce.entity.OrderItem;
import jakarta.ws.rs.NotFoundException;

import java.util.UUID;

public interface OrderService {

    /**
     * 创建订单
     * @throws NotFoundException 如果用户、地址或商品不存在
     * @throws IllegalStateException 如果库存不足
     */
    OrderCreatedDto createOrder(OrderCreateDto createDto) throws NotFoundException, IllegalStateException;

    /**
     * 获取订单详情（客户）
     */
    OrderDetailResponseDto getOrderDetails(String orderId) throws NotFoundException;

    /**
     * 更新订单（客户/管理员）
     */
    OrderDetailResponseDto updateOrder(OrderUpdateDto updateDto) throws NotFoundException;

    /**
     * 客户更新订单项状态
     */
    OrderItem updateCustomerItemStatus(OrderItemStatusUpdateDto updateDto) throws NotFoundException, IllegalArgumentException;

    /**
     * 获取客户订单列表（分页）
     */
    PaginatedResult<OrderListDto> getOrdersByUserId(String userId, String itemStatus, int page, int pageSize);

    /**
     * 获取订单项详情（用于评论检查）
     */
    OrderItemCommentStatusDto getOrderItemForComment(String itemId) throws NotFoundException;

    /**
     * 获取未读通知数量
     */
    OrderNotificationCountDto getNotificationCount(String userId);

    /**
     * 标记通知为已读
     */
    void markNotificationsAsRead(String userId);

    /**
     * 获取首页的消息计数
     * @param userId
     * @return
     */
    HomeMessageCountDto getMessageCountsByUserId(String userId);

    /**
     * 管理员：获取所有订单列表（分页）
     * @param itemStatus
     * @param page
     * @param pageSize
     * @return
     */
    PaginatedResult<OrderListDto> getAdminOrdersList(String itemStatus, int page, int pageSize);

    /**
     * 管理员：获取订单详情（与客户视图相同）
     * @param orderId
     * @return
     * @throws NotFoundException
     */
    OrderDetailResponseDto getAdminOrderDetail(String orderId) throws NotFoundException;

    /**
     * 管理员：更新订单项状态（例如：发货、退款完成）
     * @param updateDto
     * @return
     * @throws NotFoundException
     * @throws IllegalArgumentException
     */
    OrderItem updateAdminItemStatus(AdminOrderItemStatusUpdateDto updateDto) throws NotFoundException, IllegalArgumentException;

}