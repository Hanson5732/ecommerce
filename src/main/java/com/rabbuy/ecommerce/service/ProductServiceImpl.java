package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dao.ProductDao;
import com.rabbuy.ecommerce.dao.SubCategoryDao;
import com.rabbuy.ecommerce.dto.*;
import com.rabbuy.ecommerce.entity.Product;
import com.rabbuy.ecommerce.entity.SubCategory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductServiceImpl implements ProductService {

    @Inject
    private ProductDao productDao;

    @Inject
    private SubCategoryDao subCategoryDao;

    @Override
    public ProductDetailDto getProductDetails(UUID productId) throws NotFoundException {
        // DAO 方法已包含 status='1' 和 is_deleted=False 检查
        Product product = productDao.findActiveById(productId)
                .orElseThrow(() -> new NotFoundException("Product does not exist")); //

        // 使用 DTO 的工厂方法进行转换
        return ProductDetailDto.fromEntity(product);
    }

    @Override
    public List<ProductListDto> getLatestProducts(int limit) {
        // DAO 方法已包含 status='1', is_deleted=False 和排序
        return productDao.findLatestActiveProducts(limit).stream()
                .map(ProductListDto::fromHomeView) // 使用简化的 DTO
                .collect(Collectors.toList());
    }


    private static final double MIN_HOT_RATING = 3.75;

    @Override
    public List<ProductListDto> getHotProducts(int limit) {
        // DAO 方法已包含 status='1', is_deleted=False 和评分过滤
        return productDao.findHotActiveProducts(MIN_HOT_RATING, limit).stream()
                .map(ProductListDto::fromHomeView) // 使用简化的 DTO
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResult<ProductListDto> searchProducts(ProductSearchCriteria criteria) {
        // DAO 层已处理所有复杂的过滤、分类、价格和排序逻辑
        PaginatedResult<Product> entityResult = productDao.findActiveByCriteria(criteria);

        // 将实体列表转换为 DTO 列表
        List<ProductListDto> dtoList = entityResult.data().stream()
                .map(ProductListDto::fromEntity) // 使用完整的 DTO
                .collect(Collectors.toList());

        // 返回 DTO 格式的分页结果
        return new PaginatedResult<>(
                dtoList,
                entityResult.totalItems(),
                entityResult.currentPage(),
                entityResult.totalPages()
        );
    }

    @Override
    public List<ProductListDto> getProductRecommendations(UUID productId, String name, int limit) {
        if (name == null || name.isEmpty()) {
            return List.of(); //
        }

        String keyword = name.split(" ")[0];

        List<Product> products = productDao.findActiveRecommendations(keyword, productId, limit);

        return products.stream()
                .map(ProductListDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ProductStatusDto getProductStatus(UUID productId) throws NotFoundException {
        // 此接口需要检查任何商品，无论其状态如何，因此我们使用 findById
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        return ProductStatusDto.fromEntity(product);
    }

    @Override
    public ProductStockStatus getProductStockStatus() {
        //
        return productDao.getStockStatus();
    }

    @Override
    public PaginatedResult<ProductAdminListDto> getAdminProductList(String query, int page, int pageSize) {
        //
        PaginatedResult<Product> entityResult = productDao.findAdminByKeyword(query, page, pageSize);

        List<ProductAdminListDto> dtoList = entityResult.data().stream()
                .map(ProductAdminListDto::fromEntity)
                .collect(Collectors.toList());

        return new PaginatedResult<>(
                dtoList,
                entityResult.totalItems(),
                entityResult.currentPage(),
                entityResult.totalPages()
        );
    }

    @Override
    public ProductDetailDto getAdminProductDetails(UUID productId) throws NotFoundException {
        //
        // 使用 findById (不过滤 status/is_deleted)
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product does not exist"));

        return ProductDetailDto.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductDetailDto addProduct(ProductAdminInputDto dto) throws NotFoundException, IllegalArgumentException {
        //
        // 1. 验证
        if (dto.name() == null || dto.price() == null || dto.subCategoryId() == null) {
            throw new IllegalArgumentException("Missing required field: name, price, or subCategoryId");
        }

        // 2. 查找子分类
        SubCategory subCategory = subCategoryDao.findById(dto.subCategoryId())
                .orElseThrow(() -> new NotFoundException("SubCategory not found"));

        // 3. 创建实体
        Product product = new Product();
        product.setProductName(dto.name());
        product.setPrice(dto.price());
        product.setProductDesc(dto.description() != null ? dto.description() : "");
        product.setStockQuantity(dto.stockQuantity() != null ? dto.stockQuantity() : 0);
        product.setLowStockThreshold(dto.lowStockThreshold() != null ? dto.lowStockThreshold() : 0);
        product.setImages(dto.images() != null ? dto.images() : new ArrayList<>());
        product.setStatus(dto.status() != null ? dto.status() : "0");
        product.setSubCategory(subCategory);
        product.setProductRating(0.0); // 默认值
        product.setRatingNum(0); // 默认值

        // 4. 【妥协】转换 Details DTO
        // Django 过滤空值
        if (dto.details() != null) {
            List<String> detailsAsStrings = dto.details().stream()
                    .filter(d -> d.key() != null && !d.key().isEmpty() && d.value() != null && !d.value().isEmpty())
                    // 转换为 "Key: Value" 字符串，以匹配 List<String> 实体
                    .map(d -> d.key() + ": " + d.value())
                    .collect(Collectors.toList());
            product.setProductDetails(detailsAsStrings);
        } else {
            product.setProductDetails(new ArrayList<>());
        }

        // 5. 保存 (PrePersist 会设置 created_time)
        productDao.save(product);

        return ProductDetailDto.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductDetailDto updateProduct(UUID productId, ProductAdminInputDto dto) throws NotFoundException, IllegalArgumentException {
        //
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // 更新字段
        if (dto.name() != null) product.setProductName(dto.name());
        if (dto.price() != null) product.setPrice(dto.price());
        if (dto.description() != null) product.setProductDesc(dto.description());

        // Django 逻辑 是 *增加* 库存
        if (dto.stockQuantity() != null) {
            product.setStockQuantity(product.getStockQuantity() + dto.stockQuantity());
        }

        if (dto.lowStockThreshold() != null) product.setLowStockThreshold(dto.lowStockThreshold());
        if (dto.images() != null) product.setImages(dto.images());
        if (dto.status() != null) product.setStatus(dto.status());

        // 更新子分类
        if (dto.subCategoryId() != null && !dto.subCategoryId().equals(product.getSubCategory().getSubCateId())) {
            SubCategory subCategory = subCategoryDao.findById(dto.subCategoryId())
                    .orElseThrow(() -> new NotFoundException("SubCategory not found"));
            product.setSubCategory(subCategory);
        }

        // 更新 Details
        if (dto.details() != null) {
            List<String> detailsAsStrings = dto.details().stream()
                    .filter(d -> d.key() != null && !d.key().isEmpty() && d.value() != null && !d.value().isEmpty())
                    .map(d -> d.key() + ": " + d.value())
                    .collect(Collectors.toList());
            product.setProductDetails(detailsAsStrings);
        }

        // 5. 更新 (PreUpdate 会设置 updated_time)
        // (在事务中，JPA 会自动保存更改，但显式调用 update 也无妨)
        productDao.update(product);

        return ProductDetailDto.fromEntity(product);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId) throws NotFoundException {
        //
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product does not exist"));

        // 逻辑删除
        productDao.logicalDelete(product); //
    }
}