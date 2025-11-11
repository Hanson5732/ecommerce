package com.rabbuy.ecommerce.dto;

// 对应 update_order_view
public record OrderUpdateDto(
        String orderId,
        String deliveryTime,
        String address, // addressId
        String orderStatus
) {
}