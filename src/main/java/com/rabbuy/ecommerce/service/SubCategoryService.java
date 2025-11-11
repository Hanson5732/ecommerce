package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.dto.SubCategoryDto;
import com.rabbuy.ecommerce.dto.SubCategoryInputDto;
import com.rabbuy.ecommerce.dto.SubCategoryProductDto;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SubCategoryService {

    /**
     * 获取二级分类详情
     */
    SubCategoryDto getSubCategoryDetails(String id) throws NotFoundException;

    /**
     * 获取二级分类下的商品（分页）
     */
    PaginatedResult<SubCategoryProductDto> getSubCategoryProducts(
            String subCategoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String sortField, int page, int pageSize) throws NotFoundException;

    /**
     * 获取所有二级分类（用于管理后台列表）
     */
    List<SubCategoryDto> getAllSubCategories();

    /**
     * 分页获取二级分类（用于管理后台）
     */
    PaginatedResult<SubCategoryDto> getAdminSubCategories(int page, int pageSize);

    /**
     * 添加二级分类
     */
    SubCategoryDto addSubCategory(SubCategoryInputDto dto) throws NotFoundException, IllegalArgumentException;

    /**
     * 更新二级分类
     */
    SubCategoryDto updateSubCategory(String id, SubCategoryInputDto dto) throws NotFoundException, IllegalArgumentException;

    /**
     * 删除二级分类
     */
    void deleteSubCategory(String id) throws NotFoundException, IllegalStateException;
}