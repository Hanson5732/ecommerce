package com.rabbuy.ecommerce.dto;

import com.rabbuy.ecommerce.entity.SubCategory;

import java.util.UUID;

/**
 * 用于 SubCategory API 响应的 DTO
 *
 */
public record SubCategoryDto(
        String id,
        String name,
        String status,
        String imageUrl,
        String categoryId,
        String categoryName
) {
    /**
     * 工厂方法：从 SubCategory 实体转换为 DTO
     */
    public static SubCategoryDto fromEntity(SubCategory entity) {
        if (entity == null) {
            return null;
        }

        String catId = null;
        String catName = null;

        // 确保父分类被加载（如果它是 LAZY）
        if (entity.getCategory() != null) {
            catId = entity.getCategory().getCategoryId();
            catName = entity.getCategory().getCategoryName();
        }

        return new SubCategoryDto(
                entity.getSubCateId(),
                entity.getSubCateName(),
                entity.getStatus(),
                entity.getSubCateImage(),
                catId,
                catName
        );
    }
}