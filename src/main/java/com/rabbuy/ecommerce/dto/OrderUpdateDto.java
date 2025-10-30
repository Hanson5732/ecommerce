package com.rabbuy.ecommerce.dto;

import java.util.UUID;

// 对应 update_order_view
public record OrderUpdateDto(
        UUID orderId,
        String deliveryTime,
        UUID address, // addressId
        String orderStatus
) {
}