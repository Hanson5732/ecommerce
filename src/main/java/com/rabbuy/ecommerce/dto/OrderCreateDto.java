package com.rabbuy.ecommerce.dto;

import java.util.List;
import java.util.UUID;

public record OrderCreateDto(
        UUID userId,
        UUID addressId,
        String deliveryTime, // '0', '1', or '2'
        List<CartItem> products // 重用 CartItem POJO (id, count)
) {
}