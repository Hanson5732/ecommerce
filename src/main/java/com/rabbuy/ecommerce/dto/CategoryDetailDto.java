package com.rabbuy.ecommerce.dto;

import java.util.List;

public record CategoryDetailDto(
        String id,
        String name,
        List<String> images,          // 一级分类的图片列表
        List<SubCategoryDetail> subcate // 包含的二级分类列表
) {
    /**
     * 内部嵌套记录：二级分类详情（包含商品）
     */
    public record SubCategoryDetail(
            String id,
            String name,
            String image,             // 二级分类图片
            List<ProductListDto> products // 二级分类下的推荐商品
    ) {}
}
