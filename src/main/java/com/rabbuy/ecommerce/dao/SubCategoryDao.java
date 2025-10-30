package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.SubCategory;
import java.util.List;
import java.util.UUID;

public interface SubCategoryDao {

    // 统计某个主分类下的子分类数量
    long countByCategoryId(UUID categoryId);

}