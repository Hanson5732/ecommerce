package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;

public record CartDetailItemDto(
        String id,
        String name,
        int count,
        BigDecimal price, // 实时价格
        String image,     // 实时图片
        boolean status,   // 实时状态 (是否已删除, 是否可用, 是否有库存)
        boolean selected  // 实时状态 (用于前端勾选)
) {
}