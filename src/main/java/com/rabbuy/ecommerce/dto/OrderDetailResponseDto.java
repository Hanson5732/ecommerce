package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


public record OrderDetailResponseDto(
        UUID id, // order_id
        String deliveryTime,
        List<OrderItemResponseDto> products,
        String orderStatus,
        BigDecimal amount,
        OffsetDateTime createdTime
) {
}