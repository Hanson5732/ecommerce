package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;
import java.util.UUID;


public record HomeProductGoodsDto(
        UUID id,
        String name,
        BigDecimal price,
        String images
) {}