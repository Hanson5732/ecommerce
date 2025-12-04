package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dao.CategoryDao;
import com.rabbuy.ecommerce.dao.ProductDao;
import com.rabbuy.ecommerce.dao.SubCategoryDao;
import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.dto.SubCategoryDto;
import com.rabbuy.ecommerce.dto.SubCategoryInputDto;
import com.rabbuy.ecommerce.dto.SubCategoryProductDto;
import com.rabbuy.ecommerce.entity.Category;
import com.rabbuy.ecommerce.entity.Product;
import com.rabbuy.ecommerce.entity.SubCategory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class SubCategoryServiceImpl implements SubCategoryService {

    @Inject
    private SubCategoryDao subCategoryDao;
    @Inject
    private CategoryDao categoryDao;
    @Inject
    private ProductDao productDao;

    @Override
    @Transactional
    public SubCategoryDto getSubCategoryDetails(String id) throws NotFoundException {
        SubCategory subCategory = subCategoryDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Sub-category not found"));
        return SubCategoryDto.fromEntity(subCategory);
    }

    @Override
    public PaginatedResult<SubCategoryProductDto> getSubCategoryProducts(
            String subCategoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String sortField, int page, int pageSize) throws NotFoundException {

        // 1. 验证 SubCategory 是否存在
        if (!subCategoryDao.findById(subCategoryId).isPresent()) {
            throw new NotFoundException("Subcategory not found");
        }

        // 2. 调用 ProductDao 中已有的方法
        //
        PaginatedResult<Product> entityResult = productDao.findActiveBySubCategory(
                subCategoryId, minPrice, maxPrice, sortField, page, pageSize
        );

        // 3. 将 PaginatedResult<Product> 转换为 PaginatedResult<SubCategoryProductDto>
        List<SubCategoryProductDto> dtoList = entityResult.data().stream()
                .map(SubCategoryProductDto::fromEntity) // 使用新 DTO 的工厂方法
                .collect(Collectors.toList());

        return new PaginatedResult<>(
                dtoList,
                entityResult.totalItems(),
                entityResult.currentPage(),
                entityResult.totalPages()
        );
    }

    @Override
    public List<SubCategoryDto> getAllSubCategories() {
        //
        return subCategoryDao.findAll().stream()
                .map(SubCategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResult<SubCategoryDto> getAdminSubCategories(int page, int pageSize) {
        //
        PaginatedResult<SubCategory> entityResult = subCategoryDao.findAllPaginated(page, pageSize);

        List<SubCategoryDto> dtoList = entityResult.data().stream()
                .map(SubCategoryDto::fromEntity)
                .collect(Collectors.toList());

        return new PaginatedResult<>(
                dtoList,
                entityResult.totalItems(),
                entityResult.currentPage(),
                entityResult.totalPages()
        );
    }

    @Override
    @Transactional
    public SubCategoryDto addSubCategory(SubCategoryInputDto dto) throws NotFoundException, IllegalArgumentException {
        //
        if (dto.name() == null || dto.name().trim().isEmpty() || dto.categoryId() == null) {
            throw new IllegalArgumentException("Subcategory name and category ID are required");
        }

        Category parentCategory = categoryDao.findById(dto.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        SubCategory newSubCategory = new SubCategory();
        newSubCategory.setSubCateName(dto.name());
        newSubCategory.setCategory(parentCategory);
        newSubCategory.setSubCateImage(dto.images() != null ? dto.images() : "");
        newSubCategory.setStatus(dto.status() != null ? dto.status() : "0"); // 默认 '0'

        subCategoryDao.save(newSubCategory);
        return SubCategoryDto.fromEntity(newSubCategory);
    }

    @Override
    @Transactional
    public SubCategoryDto updateSubCategory(String id, SubCategoryInputDto dto) throws NotFoundException, IllegalArgumentException {
        //
        SubCategory subCategory = subCategoryDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Subcategory not found"));

        if (dto.name() != null && !dto.name().trim().isEmpty()) {
            subCategory.setSubCateName(dto.name());
        }
        if (dto.status() != null) {
            subCategory.setStatus(dto.status());
        }
        if (dto.images() != null) {
            subCategory.setSubCateImage(dto.images());
        }
        if (dto.categoryId() != null && !dto.categoryId().equals(subCategory.getCategory().getCategoryId())) {
            Category parentCategory = categoryDao.findById(dto.categoryId())
                    .orElseThrow(() -> new NotFoundException("New Category not found"));
            subCategory.setCategory(parentCategory);
        }

        SubCategory updated = subCategoryDao.update(subCategory);
        return SubCategoryDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteSubCategory(String id) throws NotFoundException, IllegalStateException {
        //
        if (!subCategoryDao.findById(id).isPresent()) {
            throw new NotFoundException("Subcategory not found");
        }

        // 检查是否有商品关联
        //
        PaginatedResult<Product> products = productDao.findActiveBySubCategory(id, null, null, "default", 1, 1);
        if (products.totalItems() > 0) {
            throw new IllegalStateException("Cannot delete subcategory with associated products");
        }

        // DAO 的 deleteById 需要一个实体，我们直接用 ID 查找
        SubCategory subCategory = subCategoryDao.findById(id).get();
        subCategoryDao.delete(subCategory);
    }
}