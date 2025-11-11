package com.rabbuy.ecommerce.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record CartResponseDto(
        String id, // cart_id
        String user, // user_id
        List<CartDetailItemDto> products,
        OffsetDateTime createdTime,
        OffsetDateTime updatedTime
) {
}