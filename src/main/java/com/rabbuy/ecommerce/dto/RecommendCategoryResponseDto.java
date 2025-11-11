package com.rabbuy.ecommerce.dto;

import java.util.List;

// 对应 Python 'category_data'
public record RecommendCategoryResponseDto(
        String id,
        String name,
        List<RecommendSubCategoryDto> children,
        List<RecommendProductDto> products
) {}