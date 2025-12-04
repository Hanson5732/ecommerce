package com.rabbuy.ecommerce.service;


import com.rabbuy.ecommerce.dao.CategoryDao;
import com.rabbuy.ecommerce.dao.ProductDao;
import com.rabbuy.ecommerce.dao.SubCategoryDao;
import com.rabbuy.ecommerce.dto.*;
import com.rabbuy.ecommerce.entity.Category;
import com.rabbuy.ecommerce.entity.Product;
import com.rabbuy.ecommerce.entity.SubCategory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped // 声明为 CDI Bean
public class CategoryServiceImpl implements CategoryService {

    @Inject
    private CategoryDao categoryDao;

    @Inject
    private SubCategoryDao subCategoryDao;

    @Inject
    private ProductDao productDao;

    // 辅助方法：将 Entity 转换为 DTO
    private CategoryAdminDto toAdminDto(Category entity) {
        return new CategoryAdminDto(
                entity.getCategoryId(),
                entity.getCategoryName(),
                entity.getStatus(),
                entity.getCategoryImages()
        );
    }

    /**
     * 获取导航栏分类列表
     * 对应 Python: get_category_nav_view
     * 修复：使用 DAO 中已存在的 findActiveCategories(7) 方法
     */
    @Override
    public List<CategoryNavDto> getNavigationCategories() {
        // 直接调用 DAO 的方法获取前 7 个启用分类
        return categoryDao.findActiveCategories(7).stream()
                .map(this::toNavDto)
                .collect(Collectors.toList());
    }

    /**
     * 辅助方法：转换为简单的导航 DTO (只包含 id 和 name)
     */
    private CategoryNavDto toNavDto(Category entity) {
        return new CategoryNavDto(
                entity.getCategoryId(),
                entity.getCategoryName()
        );
    }

    @Override
    public PaginatedResult<CategoryAdminDto> getAdminCategories(int page, int pageSize) {
        // 1. 调用 DAO 获取分页的实体列表
        PaginatedResult<Category> entityResult = categoryDao.findAll(page, pageSize);

        // 2. 将实体列表转换为 DTO 列表
        List<CategoryAdminDto> dtoList = entityResult.data().stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());

        // 3. 构造并返回新的分页结果
        return new PaginatedResult<>(
                dtoList,
                entityResult.totalItems(),
                entityResult.currentPage(),
                entityResult.totalPages()
        );
    }

    @Override
    public Optional<CategoryAdminDto> getCategoryById(String id) {
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
    public CategoryAdminDto updateCategory(String id, CategoryInputDto categoryDto) {
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
    public void deleteCategory(String id) throws IllegalStateException {
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

    @Override
    public CategoryDetailDto getCategoryDetails(String categoryId) {
        // 1. 获取一级分类
        Category category = categoryDao.findById(categoryId)
                .filter(c -> "1".equals(c.getStatus()))
                .orElseThrow(() -> new NotFoundException("Category not found or inactive"));

        // 2. 获取该分类下的所有启用二级分类
        List<SubCategory> allSubCategories = subCategoryDao.findActiveByCategoryId(categoryId);

        // 3. 截取前 7 个
        List<SubCategory> limitedSubCats = allSubCategories.stream()
                .limit(7)
                .collect(Collectors.toList());

        // 4. 构造二级分类 DTO 列表
        List<CategoryDetailDto.SubCategoryDetail> subCategoryDetails = limitedSubCats.stream().map(sub -> {

            // 4.1 获取该二级分类下的所有可用商品
            List<Product> products = productDao.findActiveBySubCategoryForRandom(sub.getSubCateId());

            // 4.2 随机打乱并取前 4 个
            Collections.shuffle(products);
            List<Product> randomProducts = products.stream()
                    .limit(4)
                    .collect(Collectors.toList());

            // 4.3 转换为 ProductListDto (修复点：使用 fromHomeView 方法)
            List<ProductListDto> productDtos = randomProducts.stream()
                    .map(ProductListDto::fromHomeView) // 使用现有的静态方法进行转换
                    .collect(Collectors.toList());

            // 4.4 返回二级分类内部 DTO
            return new CategoryDetailDto.SubCategoryDetail(
                    sub.getSubCateId(),
                    sub.getSubCateName(),
                    sub.getSubCateImage(),
                    productDtos
            );
        }).collect(Collectors.toList());

        // 5. 返回最终的一级分类详情 DTO
        return new CategoryDetailDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCategoryImages(),
                subCategoryDetails
        );
    }
}