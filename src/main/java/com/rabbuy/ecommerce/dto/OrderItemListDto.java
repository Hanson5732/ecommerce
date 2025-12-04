package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// 对应 get_order_by_user_id_view 中 items 列表的元素
public record OrderItemListDto(
        String id, // item_id
        String productId,
        String itemStatus,
        String name,
        String image,
        OffsetDateTime createdTime,
        OffsetDateTime updatedTime,
        BigDecimal price,
        int quantity
) {}