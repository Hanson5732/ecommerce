package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;


public record RecommendProductDto(
        String id,
        String name,
        BigDecimal price,
        String image
) {}