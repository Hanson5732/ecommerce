package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.entity.SubCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubCategoryDao {

    /**
     * 根据 ID 查找
     */
    Optional<SubCategory> findById(UUID id);

    /**
     * 查找所有
     */
    List<SubCategory> findAll();

    /**
     * 分页查找所有 (用于管理后台)
     * 对应 Django: get_admin_subcategory_view
     *
     */
    PaginatedResult<SubCategory> findAllPaginated(int page, int pageSize);

    /**
     * 统计某个主分类下的子分类数量
     * (用于 CategoryService.deleteCategory 检查)
     */
    long countByCategoryId(UUID categoryId);

    /**
     * 保存 (新增)
     */
    void save(SubCategory subCategory);

    /**
     * 更新
     */
    SubCategory update(SubCategory subCategory);

    /**
     * 删除
     */
    void delete(SubCategory subCategory);
}