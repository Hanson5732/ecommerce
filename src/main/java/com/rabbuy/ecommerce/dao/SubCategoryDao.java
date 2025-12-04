package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.entity.SubCategory;
import java.util.List;
import java.util.Optional;

public interface SubCategoryDao {

    /**
     * 根据 ID 查找
     */
    Optional<SubCategory> findById(String id);

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
    long countByCategoryId(String categoryId);

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

    /**
     * 查找所有状态为 '1' (启用) 的子分类
     * @return
     */
    List<SubCategory> findAllActive();

    /**
     * 查找某个主分类下的所有启用状态的子分类
     * @param categoryId
     * @return
     */
    List<SubCategory> findActiveByCategoryId(String categoryId);

    /**
     * 根据一级分类ID查找有效的二级分类，并限制数量
     * @param categoryId
     * @param limit
     * @return
     */
    List<SubCategory> findByCategoryId(String categoryId, int limit);
}