package com.rabbuy.ecommerce.dto;

import com.rabbuy.ecommerce.entity.Product;
import java.math.BigDecimal;

public record ProductListDto(
        String id,
        String name,
        String description, // 搜索时需要
        BigDecimal price,
        String image, // 仅第一张图片
        String subCategory, // 分类名称
        String category     // 主分类名称
) {
    /**
     * 工厂方法：用于搜索和推荐列表
     */
    public static ProductListDto fromEntity(Product product) {
        String imageUrl = (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null;
        String subCategoryName = product.getSubCategory() != null ? product.getSubCategory().getSubCateName() : "Unknown";
        String categoryName = (product.getSubCategory() != null && product.getSubCategory().getCategory() != null)
                ? product.getSubCategory().getCategory().getCategoryName()
                : "Unknown";

        return new ProductListDto(
                product.getProductId(),
                product.getProductName(),
                product.getProductDesc(), //
                product.getPrice(),
                imageUrl,
                subCategoryName,
                categoryName
        );
    }


    public static ProductListDto fromHomeView(Product product) {
        String imageUrl = (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null;

        return new ProductListDto(
                product.getProductId(),
                product.getProductName(),
                null, // 首页不需要描述
                product.getPrice(),
                imageUrl,
                null, // 首页不需要分类名
                null
        );
    }
}