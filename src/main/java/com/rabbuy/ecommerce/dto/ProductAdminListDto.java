package com.rabbuy.ecommerce.dto;

import com.rabbuy.ecommerce.entity.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ProductAdminListDto(
        String id,
        String name,
        BigDecimal price,
        String status,
        Integer stockQuantity,
        Integer lowStockThreshold,
        String description,
        Double rating,
        List<String> images,
        String subcategory,
        String category,
        OffsetDateTime createdTime,
        OffsetDateTime updatedTime
) {
    /**
     * 工厂方法：从 Product 实体转换为 DTO
     */
    public static ProductAdminListDto fromEntity(Product product) {
        if (product == null) {
            return null;
        }

        String subCatName = (product.getSubCategory() != null)
                ? product.getSubCategory().getSubCateName() : null;
        String catName = (product.getSubCategory() != null && product.getSubCategory().getCategory() != null)
                ? product.getSubCategory().getCategory().getCategoryName() : null;

        return new ProductAdminListDto(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                product.getStatus(),
                product.getStockQuantity(),
                product.getLowStockThreshold(),
                product.getProductDesc(),
                product.getProductRating(),
                product.getImages(),
                subCatName,
                catName,
                product.getCreatedTime(),
                product.getUpdatedTime()
        );
    }
}