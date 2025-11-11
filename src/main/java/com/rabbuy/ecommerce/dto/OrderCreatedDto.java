package com.rabbuy.ecommerce.dto;

public record OrderCreatedDto(
        String id, // order_id
        String deliveryTime,
        String user,
        String address,
        String orderStatus
) {
}