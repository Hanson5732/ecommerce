package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductAdminInputDto(
        String name,
        BigDecimal price,
        String description,
        Integer stockQuantity,
        Integer lowStockThreshold,
        List<String> images,
        String status, // "0" or "1"
        String subCategoryId,
        List<DetailDto> details
) {
    /**
     * 嵌套 DTO，用于 product_details
     *
     */
    public record DetailDto(
            String key,
            String value
    ) {}
}