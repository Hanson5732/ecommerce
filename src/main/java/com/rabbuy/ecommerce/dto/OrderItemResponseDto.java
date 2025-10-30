package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// 对应 get_order_view 中 products 列表的元素
public record OrderItemResponseDto(
        UUID id, // product_id
        String name,
        BigDecimal price,
        String image,
        int count,
        String itemId, // order_item_id
        String itemStatus,
        OffsetDateTime updatedTime
) {
}