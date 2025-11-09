package com.rabbuy.ecommerce.dto;

import java.util.List;
import java.util.UUID;

// 对应 Python 'category_data'
public record RecommendCategoryResponseDto(
        UUID id,
        String name,
        List<RecommendSubCategoryDto> children,
        List<RecommendProductDto> products
) {}