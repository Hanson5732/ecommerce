package com.rabbuy.ecommerce.dto;

import java.util.List;

public record OrderCreateDto(
        String userId,
        String addressId,
        String deliveryTime, // '0', '1', or '2'
        List<CartItem> products // 重用 CartItem POJO (id, count)
) {
}