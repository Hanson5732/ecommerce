package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;


public record HomeProductGoodsDto(
        String id,
        String name,
        BigDecimal price,
        String images
) {}