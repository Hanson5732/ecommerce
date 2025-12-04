package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// 对应 get_order_view 中 products 列表的元素
public record OrderItemResponseDto(
        String id, // product_id
        String name,
        BigDecimal price,
        String image,
        int count,
        String itemId,
        String itemStatus,
        OffsetDateTime updatedTime
) {
}