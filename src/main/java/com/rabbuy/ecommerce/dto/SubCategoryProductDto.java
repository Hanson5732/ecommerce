package com.rabbuy.ecommerce.dto;

import com.rabbuy.ecommerce.entity.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 用于 SubCategory 详情页的商品列表 DTO
 *
 */
public record SubCategoryProductDto(
        UUID id,
        String name,
        BigDecimal price,
        String image,
        OffsetDateTime createdTime
) {
    /**
     * 工厂方法：从 Product 实体转换为 DTO
     */
    public static SubCategoryProductDto fromEntity(Product product) {
        String imageUrl = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0)
                : null;

        return new SubCategoryProductDto(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                imageUrl,
                product.getCreatedTime() //
        );
    }
}