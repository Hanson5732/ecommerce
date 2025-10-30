package com.rabbuy.ecommerce.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CartResponseDto(
        UUID id, // cart_id
        UUID user, // user_id
        List<CartDetailItemDto> products,
        OffsetDateTime createdTime,
        OffsetDateTime updatedTime
) {
}