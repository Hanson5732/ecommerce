package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;


public record OrderDetailResponseDto(
        String id,
        String deliveryTime,
        List<OrderItemResponseDto> products,
        String orderStatus,
        BigDecimal amount,
        OffsetDateTime createdTime,

        String userId,

        String username,

        AddressDto address
) {
}