package com.rabbuy.ecommerce.dto;

/**
 * 用于管理员更新订单项状态
 * @param itemId
 * @param status
 */
public record AdminOrderItemStatusUpdateDto(
        String itemId,
        String status
) {
}
