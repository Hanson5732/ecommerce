package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.*;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    /**
     * 获取导航栏分类
     */
    List<CategoryNavDto> getNavigationCategories();

    /**
     * 获取管理员后台分类列表（分页）
     */
    PaginatedResult<CategoryAdminDto> getAdminCategories(int page, int pageSize); // (暂未实现分页)

    /**
     * 根据 ID 查找
     */
    Optional<CategoryAdminDto> getCategoryById(String id);

    /**
     * 添加新分类
     */
    CategoryAdminDto addCategory(CategoryInputDto categoryDto);

    /**
     * 更新分类
     */
    CategoryAdminDto updateCategory(String id, CategoryInputDto categoryDto);

    /**
     * 删除分类
     * @throws IllegalStateException 如果分类下有子分类
     */
    void deleteCategory(String id) throws IllegalStateException;

    /**
     * 根据 ID 获取分类详细信息（包含子分类及商品）
     *
     * @param categoryId
     * @return
     */
    CategoryDetailDto getCategoryDetails(String categoryId);
}