package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.dto.ProductSearchCriteria;
import com.rabbuy.ecommerce.dto.ProductStockStatus;
import com.rabbuy.ecommerce.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.*;

@ApplicationScoped
public class ProductDaoImpl implements ProductDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    // --- 基本 CRUD ---

    @Override
    @Transactional
    public void save(Product product) {
        if (product.getProductId() == null) {
            em.persist(product);
        } else {
            em.merge(product);
        }
    }

    @Override
    public Optional<Product> findById(String id) {
        // 查找包括已删除或不可用的产品 (供管理员使用)
        return Optional.ofNullable(em.find(Product.class, id));
    }

    @Override
    @Transactional
    public void update(Product product) {
        if (product.getProductId() != null && em.find(Product.class, product.getProductId()) != null) {
            em.merge(product);
        } else {
            throw new IllegalArgumentException("Product with id " + product.getProductId() + " not found for update.");
        }
    }

    @Override
    public Optional<Product> findAdminById(String id) {
        // 使用 JOIN FETCH 预加载 SubCategory 和 Category
        String jpql = "SELECT p FROM Product p " +
                "LEFT JOIN FETCH p.subCategory s " +
                "LEFT JOIN FETCH s.category c " +
                "WHERE p.productId = :id";
        try {
            return Optional.of(
                    em.createQuery(jpql, Product.class)
                            .setParameter("id", id)
                            .getSingleResult()
            );
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }


    @Override
    @Transactional
    public void logicalDelete(Product product) {
        // 实现逻辑删除
        product.setDeleted(true);
        em.merge(product);
    }

    // --- 首页查询 ---

    @Override
    public List<Product> findLatestActiveProducts(int limit) {
        // 对应 new_view
        String jpql = "SELECT p FROM Product p WHERE p.status = '1' AND p.isDeleted = false ORDER BY p.createdTime DESC";
        return em.createQuery(jpql, Product.class)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Product> findHotActiveProducts(double minRating, int limit) {
        // 对应 hot_view, Django '?' (random) 在 JPQL 中不易实现，改为按评分排序
        String jpql = "SELECT p FROM Product p WHERE p.status = '1' AND p.isDeleted = false AND p.productRating >= :minRating ORDER BY p.productRating DESC, p.ratingNum DESC";
        return em.createQuery(jpql, Product.class)
                .setParameter("minRating", minRating)
                .setMaxResults(limit)
                .getResultList();
        // 如果必须随机，需要获取更多结果后在 Java 中 shuffle，或者使用 native query
    }

    // --- 详情页查询 ---

    @Override
    public Optional<Product> findActiveById(String id) {
        // 对应 get_details_view
        String jpql = "SELECT p FROM Product p WHERE p.productId = :id AND p.status = '1' AND p.isDeleted = false ";
        try {
            return Optional.of(
                    em.createQuery(jpql, Product.class)
                            .setParameter("id", id)
                            .getSingleResult()
            );
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findActiveRecommendations(String keyword, String excludeProductId, int limit) {
        String jpql = "SELECT p FROM Product p " +
                "LEFT JOIN FETCH p.subCategory s " +
                "LEFT JOIN FETCH s.category " +
                "WHERE p.productId != :excludeId AND p.status = '1' AND p.isDeleted = false " +
                "AND (p.productName LIKE :keyword OR p.productDesc LIKE :keyword)";

        return em.createQuery(jpql, Product.class)
                .setParameter("excludeId", excludeProductId)
                .setParameter("keyword", "%" + keyword.toLowerCase() + "%")
                .setMaxResults(limit)
                .getResultList();
    }


    // --- 搜索和列表查询 (动态 JPQL) ---

    @Override
    public PaginatedResult<Product> findActiveByCriteria(ProductSearchCriteria criteria) {
        // 对应 SearchView
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p " +
                "LEFT JOIN FETCH p.subCategory s " +
                "LEFT JOIN FETCH s.category " +
                "WHERE p.status = '1' AND p.isDeleted = false");

        StringBuilder countJpql = new StringBuilder("SELECT COUNT(p) FROM Product p WHERE p.status = '1' AND p.isDeleted = false");
        Map<String, Object> parameters = new HashMap<>();

        // 构建查询条件
        buildSearchWhereClause(jpql, countJpql, parameters, criteria.keyword(), criteria.categoryId(), criteria.minPrice(), criteria.maxPrice());

        // 构建排序
        String orderBy = buildSearchOrderBy(criteria.sortField());
        jpql.append(orderBy);

        // 创建数据查询
        TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
        // 创建总数查询
        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

        // 设置参数
        parameters.forEach(query::setParameter);
        parameters.forEach(countQuery::setParameter);

        // 获取总记录数
        long totalItems = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) totalItems / criteria.pageSize());

        // 设置分页
        query.setFirstResult((criteria.page() - 1) * criteria.pageSize());
        query.setMaxResults(criteria.pageSize());

        // 获取数据
        List<Product> data = query.getResultList();

        return new PaginatedResult<>(data, totalItems, criteria.page(), totalPages);
    }

    @Override
    public PaginatedResult<Product> findActiveBySubCategory(String subCategoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortField, int page, int pageSize) {
        // 对应 get_subcategory_products_view
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE p.status = '1' AND p.isDeleted = false AND p.subCategory.subCateId = :subCategoryId");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(p) FROM Product p WHERE p.status = '1' AND p.isDeleted = false AND p.subCategory.subCateId = :subCategoryId");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("subCategoryId", subCategoryId);

        // 构建查询条件 (仅价格)
        buildSearchWhereClause(jpql, countJpql, parameters, null, null, minPrice, maxPrice);

        // 构建排序
        String orderBy = buildSearchOrderBy(sortField);
        jpql.append(orderBy);

        // 创建查询
        TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

        // 设置参数
        parameters.forEach(query::setParameter);
        parameters.forEach(countQuery::setParameter);

        // 获取总数
        long totalItems = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // 设置分页
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        List<Product> data = query.getResultList();

        return new PaginatedResult<>(data, totalItems, page, totalPages);
    }

    @Override
    public PaginatedResult<Product> findAdminByKeyword(String keyword, int page, int pageSize) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p " +
                "LEFT JOIN FETCH p.subCategory s " +
                "LEFT JOIN FETCH s.category c " +
                "WHERE p.isDeleted = false");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(p) FROM Product p WHERE p.isDeleted = false");
        Map<String, Object> parameters = new HashMap<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            jpql.append(" AND (p.productName LIKE :keyword OR p.productId LIKE :keyword)");
            countJpql.append(" AND (p.productName LIKE :keyword OR p.productId LIKE :keyword)");
            parameters.put("keyword", likeKeyword);
        }

        jpql.append(" ORDER BY p.updatedTime DESC");

        TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

        parameters.forEach(query::setParameter);
        parameters.forEach(countQuery::setParameter);

        long totalItems = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        List<Product> data = query.getResultList();

        return new PaginatedResult<>(data, totalItems, page, totalPages);
    }

    // --- 库存查询 ---

    @Override
    public ProductStockStatus getStockStatus() {
        // 对应 get_product_inventory_status_view
        // 注意：原 Django 查询未过滤 is_deleted=false，这里保持一致。
        String lowStockJpql = "SELECT COUNT(p) FROM Product p WHERE p.stockQuantity > 0 AND p.stockQuantity < p.lowStockThreshold AND p.status = '1'";
        String outOfStockJpql = "SELECT COUNT(p) FROM Product p WHERE p.stockQuantity = 0 AND p.status = '1'";

        Long lowStockCount = em.createQuery(lowStockJpql, Long.class).getSingleResult();
        Long outOfStockCount = em.createQuery(outOfStockJpql, Long.class).getSingleResult();

        return new ProductStockStatus(outOfStockCount, lowStockCount);
    }


    // --- 动态查询辅助方法 ---

    private void buildSearchWhereClause(StringBuilder jpql, StringBuilder countJpql, Map<String, Object> parameters,
                                        String keyword, String categoryId, BigDecimal minPrice, BigDecimal maxPrice) {

        // 关键词搜索 (name 或 description)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            jpql.append(" AND (p.productName LIKE :keyword OR p.productDesc LIKE :keyword)");
            countJpql.append(" AND (p.productName LIKE :keyword OR p.productDesc LIKE :keyword)");
            parameters.put("keyword", likeKeyword);
        }

        // 按 Category ID 搜索 (需要查询子分类)
        if (categoryId != null) {
            jpql.append(" AND p.subCategory.category.categoryId = :categoryId");
            countJpql.append(" AND p.subCategory.category.categoryId = :categoryId");
            parameters.put("categoryId", categoryId);
        }

        // 价格范围
        if (minPrice != null) {
            jpql.append(" AND p.price >= :minPrice");
            countJpql.append(" AND p.price >= :minPrice");
            parameters.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            jpql.append(" AND p.price <= :maxPrice");
            countJpql.append(" AND p.price <= :maxPrice");
            parameters.put("maxPrice", maxPrice);
        }
    }

    private String buildSearchOrderBy(String sortField) {
        // 对应 SearchView 和 get_subcategory_products_view 的排序
        if (sortField == null) {
            return ""; // 默认排序或无排序
        }
        switch (sortField) {
            case "created_time":
                return " ORDER BY p.createdTime DESC";
            case "product_rating":
                return " ORDER BY p.productRating DESC";
            case "price_asc": // 假设 'price' 意味着升序
                return " ORDER BY p.price ASC";
            case "price_desc": // 添加降序选项
                return " ORDER BY p.price DESC";
            default:
                // 'default' 或其他情况不排序或按相关性（如果实现）
                return "";
        }
    }

    // ... (保留所有旧方法) ...

    @Override
    public List<Product> findActiveBySubCategoryForRandom(String subCategoryId) {
        // 仅查询，不排序不分页，后续在 Service 层 shuffle
        String jpql = "SELECT p FROM Product p WHERE p.subCategory.subCateId = :subCategoryId AND p.status = '1' AND p.isDeleted = false";
        return em.createQuery(jpql, Product.class)
                .setParameter("subCategoryId", subCategoryId)
                .getResultList();
    }
}