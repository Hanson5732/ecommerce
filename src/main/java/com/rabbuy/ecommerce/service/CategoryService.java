package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.CategoryAdminDto;
import com.rabbuy.ecommerce.dto.CategoryInputDto;
import com.rabbuy.ecommerce.dto.CategoryNavDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryService {

    /**
     * 获取导航栏分类
     */
    List<CategoryNavDto> getNavigationCategories();

    /**
     * 获取管理员后台分类列表（分页）
     */
    List<CategoryAdminDto> getAdminCategories(int page, int pageSize); // (暂未实现分页)

    /**
     * 根据 ID 查找
     */
    Optional<CategoryAdminDto> getCategoryById(UUID id);

    /**
     * 添加新分类
     */
    CategoryAdminDto addCategory(CategoryInputDto categoryDto);

    /**
     * 更新分类
     */
    CategoryAdminDto updateCategory(UUID id, CategoryInputDto categoryDto);

    /**
     * 删除分类
     * @throws IllegalStateException 如果分类下有子分类
     */
    void deleteCategory(UUID id) throws IllegalStateException;
}