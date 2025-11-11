package com.rabbuy.ecommerce.dto;

import com.rabbuy.ecommerce.entity.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;


public record ProductDetailDto(
        String id,
        String name,
        boolean status,
        BigDecimal price,
        String description,
        Double rating,
        Integer ratingNum,
        Integer stockQuantity,
        Integer lowStockThreshold,
        List<String> images,
        List<ProductDetailItem> details, // product_details
        CategoryBriefDto category, // 嵌套的分类信息
        SubCategoryBriefDto subCategory, // 嵌套的子分类信息
        OffsetDateTime createdTime,
        OffsetDateTime updatedTime
) {

    // 嵌套的 Record，用于分类信息
    public record CategoryBriefDto(String id, String name) {}
    // 嵌套的 Record，用于子分类信息
    public record SubCategoryBriefDto(String id, String name) {}


    /**
     * 工厂方法：从 Product 实体转换为 DTO
     */
    public static ProductDetailDto fromEntity(Product product) {
        if (product == null) {
            return null;
        }

        SubCategoryBriefDto subCategoryDto = new SubCategoryBriefDto(
                product.getSubCategory().getSubCateId(),
                product.getSubCategory().getSubCateName()
        );

        CategoryBriefDto categoryDto = new CategoryBriefDto(
                product.getSubCategory().getCategory().getCategoryId(),
                product.getSubCategory().getCategory().getCategoryName()
        );

        //
        boolean status = "1".equals(product.getStatus()) && !product.isDeleted();

        return new ProductDetailDto(
                product.getProductId(),
                product.getProductName(),
                status,
                product.getPrice(),
                product.getProductDesc(),
                product.getProductRating(),
                product.getRatingNum(),
                product.getStockQuantity(),
                product.getLowStockThreshold(),
                product.getImages(),
                product.getProductDetails(),
                categoryDto,
                subCategoryDto,
                product.getCreatedTime(),
                product.getUpdatedTime()
        );
    }
}