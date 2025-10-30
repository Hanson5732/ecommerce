package com.rabbuy.ecommerce.dto;

import java.util.UUID;

public record OrderCreatedDto(
        UUID id, // order_id
        String deliveryTime,
        UUID user,
        UUID address,
        String orderStatus
) {
}