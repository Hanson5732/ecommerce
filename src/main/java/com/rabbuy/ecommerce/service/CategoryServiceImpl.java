package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dao.CategoryDao;
import com.rabbuy.ecommerce.dao.SubCategoryDao;
import com.rabbuy.ecommerce.dto.CategoryAdminDto;
import com.rabbuy.ecommerce.dto.CategoryInputDto;
import com.rabbuy.ecommerce.dto.CategoryNavDto;
import com.rabbuy.ecommerce.entity.Category;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped // 声明为 CDI Bean
public class CategoryServiceImpl implements CategoryService {

    @Inject
    private CategoryDao categoryDao;

    @Inject
    private SubCategoryDao subCategoryDao;

    // 辅助方法：将 Entity 转换为 DTO
    private CategoryAdminDto toAdminDto(Category entity) {
        return new CategoryAdminDto(
                entity.getCategoryId(),
                entity.getCategoryName(),
                entity.getStatus(),
                entity.getCategoryImages()
        );
    }

    private CategoryNavDto toNavDto(Category entity) {
        return new CategoryNavDto(
                entity.getCategoryId(),
                entity.getCategoryName()
        );
    }

    @Override
    public List<CategoryNavDto> getNavigationCategories() {
        //
        return categoryDao.findActiveCategories(7).stream()
                .map(this::toNavDto) // 转换为 DTO
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryAdminDto> getAdminCategories(int page, int pageSize) {
        // TODO: 实现分页逻辑（需要修改 DAO）
        // 临时实现：返回所有
        return categoryDao.findAll().stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CategoryAdminDto> getCategoryById(UUID id) {
        return categoryDao.findById(id).map(this::toAdminDto);
    }

    @Override
    @Transactional // 此方法需要事务
    public CategoryAdminDto addCategory(CategoryInputDto categoryDto) {
        //
        if (categoryDto.name() == null || categoryDto.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        Category newCategory = new Category();
        newCategory.setCategoryName(categoryDto.name());
        newCategory.setStatus(categoryDto.status() != null ? categoryDto.status() : "0"); // 默认 "0"
        if (categoryDto.imageURL() != null) {
            newCategory.setCategoryImages(categoryDto.imageURL());
        }

        categoryDao.save(newCategory); // 保存实体

        return toAdminDto(newCategory); // 返回 DTO
    }

    @Override
    @Transactional
    public CategoryAdminDto updateCategory(UUID id, CategoryInputDto categoryDto) {
        //
        Category category = categoryDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found")); // 找不到则抛出异常

        // 更新字段
        if (categoryDto.name() != null && !categoryDto.name().trim().isEmpty()) {
            category.setCategoryName(categoryDto.name());
        }
        if (categoryDto.status() != null) {
            category.setStatus(categoryDto.status());
        }
        if (categoryDto.imageURL() != null) {
            // TODO: 在实际应用中，更新图片可能需要删除旧的 S3/文件系统中的图片
            category.setCategoryImages(categoryDto.imageURL());
        }

        // 由于 category 是受管实体 (em.find 检索到的)，
        // 在 @Transactional 方法结束时，JPA 会自动检测更改并执行 UPDATE SQL
        // 显式调用 categoryDao.update(category) (即 em.merge) 也可以，但在这里不是必须的。
        // categoryDao.update(category);

        return toAdminDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) throws IllegalStateException {
        //
        Category category = categoryDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        // 业务规则：检查是否存在关联的子分类
        long subCategoryCount = subCategoryDao.countByCategoryId(id);
        if (subCategoryCount > 0) {
            //
            throw new IllegalStateException("Cannot delete category with associated subcategories");
        }

        categoryDao.delete(category);
    }
}