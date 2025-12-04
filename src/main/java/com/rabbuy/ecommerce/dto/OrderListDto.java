package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// 对应 get_order_by_user_id_view 的 results 列表元素
public record OrderListDto(
        String id, // order_id
        OffsetDateTime createdTime, // (最新 item 的时间)
        String status, // (最高的 item 状态)
        BigDecimal totalPrice,
        int postFee, // (固定为 0)
        List<OrderItemListDto> items
) {}