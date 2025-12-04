package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.dto.ProductSearchCriteria;
import com.rabbuy.ecommerce.dto.ProductStockStatus;
import com.rabbuy.ecommerce.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Product 数据访问对象接口
public interface ProductDao {

    // --- 基本 CRUD ---
    void save(Product product);
    Optional<Product> findById(String id); // 管理员使用，查找所有
    void update(Product product);
    void logicalDelete(Product product); // 逻辑删除
    Optional<Product> findAdminById(String id);

    // --- 首页查询 ---
    List<Product> findLatestActiveProducts(int limit); // new_view
    List<Product> findHotActiveProducts(double minRating, int limit); // hot_view (按评分排序)

    // --- 详情页查询 ---
    Optional<Product> findActiveById(String id); // get_details_view
    List<Product> findActiveRecommendations(String keyword, String excludeProductId, int limit); // get_product_recommend_view

    // --- 搜索和列表查询 ---
    // 用于客户搜索 (SearchView)
    PaginatedResult<Product> findActiveByCriteria(ProductSearchCriteria criteria);

    // 用于分类页 (get_subcategory_products_view)
    PaginatedResult<Product> findActiveBySubCategory(
            String subCategoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String sortField, int page, int pageSize
    );

    // 用于管理后台 (get_product_view)
    PaginatedResult<Product> findAdminByKeyword(String keyword, int page, int pageSize);

    // --- 库存查询 ---
    ProductStockStatus getStockStatus();

    /**
     * 查找子分类下所有启用的商品（用于随机抽取）
     */
    List<Product> findActiveBySubCategoryForRandom(String subCategoryId);
}