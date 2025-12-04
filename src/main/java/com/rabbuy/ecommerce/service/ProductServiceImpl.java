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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductServiceImpl implements ProductService {

    @Inject
    private ProductDao productDao;

    @Inject
    private SubCategoryDao subCategoryDao;

    @Inject
    private CategoryDao categoryDao;

    @Override
    @Transactional
    public ProductDetailDto getProductDetails(String productId) throws NotFoundException {
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
    public List<ProductListDto> getProductRecommendations(String productId, String name, int limit) {
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
    public ProductStatusDto getProductStatus(String productId) throws NotFoundException {
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
    public ProductDetailDto getAdminProductDetails(String productId) throws NotFoundException {
        //
        // 使用 findById (不过滤 status/is_deleted)
        Product product = productDao.findAdminById(productId)
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
        if (dto.details() != null) {
            // 将 List<ProductAdminInputDto.DetailDto> 转换为 List<ProductDetailItem>
            List<ProductDetailItem> detailItems = dto.details().stream()
                    .filter(d -> d.key() != null && !d.key().isEmpty() && d.value() != null && !d.value().isEmpty())
                    // 直接创建 POJO 对象
                    .map(d -> new ProductDetailItem(d.key(), d.value()))
                    .collect(Collectors.toList());
            product.setProductDetails(detailItems); // 直接设置对象列表
        } else {
            product.setProductDetails(new ArrayList<>());
        }

        // 5. 保存 (PrePersist 会设置 created_time)
        productDao.save(product);

        return ProductDetailDto.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductDetailDto updateProduct(String productId, ProductAdminInputDto dto) throws NotFoundException, IllegalArgumentException {
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
            // 1. 不再创建 List<String>，而是创建 List<ProductDetailItem>
            List<ProductDetailItem> detailItems = dto.details().stream()
                    .filter(d -> d.key() != null && !d.key().isEmpty() && d.value() != null && !d.value().isEmpty())
                    // 2. 不再压平字符串，而是创建新 POJO
                    .map(d -> new ProductDetailItem(d.key(), d.value()))
                    .collect(Collectors.toList());

            // 3. 将 List<ProductDetailItem> 设置给实体
            product.setProductDetails(detailItems);
        }

        // 5. 更新 (PreUpdate 会设置 updated_time)
        // (在事务中，JPA 会自动保存更改，但显式调用 update 也无妨)
        productDao.update(product);

        return ProductDetailDto.fromEntity(product);
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) throws NotFoundException {
        //
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product does not exist"));

        // 逻辑删除
        productDao.logicalDelete(product); //
    }

    @Override
    public List<HomeProductResponseDto> getHomeProducts() {

        // 1. 获取所有启用的子分类 (DAO 已包含父分类)
        List<SubCategory> activeSubCategories = subCategoryDao.findAllActive();
        if (activeSubCategories.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 在内存中洗牌 (实现 Python 的 order_by('?'))
        Collections.shuffle(activeSubCategories);

        // 3. 取前 2 个 (实现 Python 的 [:2])
        List<SubCategory> randomSubCategories = activeSubCategories.stream().limit(2).collect(Collectors.toList());

        List<HomeProductResponseDto> responseData = new ArrayList<>();

        // 4. 遍历这 2 个随机子分类
        for (SubCategory subCategory : randomSubCategories) {

            // 5. 获取该分类下的所有启用商品
            List<Product> allProducts = productDao.findActiveBySubCategoryForRandom(subCategory.getSubCateId());

            // 6. 再次洗牌 (实现 Python 的 order_by('?'))
            Collections.shuffle(allProducts);

            // 7. 取前 8 个 (实现 Python 的 [:8]) 并转换为 DTO
            List<HomeProductGoodsDto> goodsList = allProducts.stream()
                    .limit(8)
                    .map(product -> new HomeProductGoodsDto(
                            product.getProductId(),
                            product.getProductName(),
                            product.getPrice(),
                            (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null
                    ))
                    .collect(Collectors.toList());

            // 8. 准备 Python 中的 "picture" 字段 (带回退URL)
            String imageUrl = (subCategory.getSubCateImage() != null && !subCategory.getSubCateImage().isEmpty())
                    ? subCategory.getSubCateImage()
                    : "https://picsum.photos/200/600"; // Fallback URL from Python code

            // 9. 构建最终的 DTO 结构
            responseData.add(new HomeProductResponseDto(
                    subCategory.getCategory().getCategoryId(),   // "id": random_subcategory.category.category_id
                    subCategory.getCategory().getCategoryName(), // "name": random_subcategory.category.category_name
                    subCategory.getSubCateName(),                // "saleInfo": random_subcategory.sub_cate_name
                    imageUrl,                                    // "picture": ...
                    goodsList                                    // "goods": [...]
            ));
        }

        return responseData;
    }

    @Override
    public List<RecommendCategoryResponseDto> getRecommendCategories() {
        // 1. 获取前7个启用状态的主分类 (Python: [7:])
        List<Category> categories = categoryDao.findActiveCategories(7);

        List<RecommendCategoryResponseDto> categoryList = new ArrayList<>();

        // 2. 遍历主分类
        for (Category category : categories) {

            // 3. 获取该主分类下的所有启用子分类
            List<SubCategory> allSubCats = subCategoryDao.findActiveByCategoryId(category.getCategoryId());

            // 4. 随机洗牌 (Python: order_by('?'))
            Collections.shuffle(allSubCats);

            // 5. 取前 2 个 (Python: [:2])
            List<SubCategory> randomSubCats = allSubCats.stream().limit(2).collect(Collectors.toList());

            List<RecommendSubCategoryDto> subListDto = new ArrayList<>();
            List<RecommendProductDto> allProductsDto = new ArrayList<>();

            // 6. 遍历 2 个随机子分类
            for (SubCategory sub : randomSubCats) {
                // 7. 构造子分类 DTO
                subListDto.add(new RecommendSubCategoryDto(sub.getSubCateId(), sub.getSubCateName()));

                // 8. 获取该子分类下的所有启用商品
                // (我们重用上一步为你创建的 findActiveBySubCategoryForRandom 方法)
                List<Product> products = productDao.findActiveBySubCategoryForRandom(sub.getSubCateId());

                // 9. 随机洗牌
                Collections.shuffle(products);

                // 10. 取前 4 个 (Python: [:4]) 并添加到总商品列表
                products.stream()
                        .limit(4)
                        .forEach(product -> {
                            allProductsDto.add(new RecommendProductDto(
                                    product.getProductId(),
                                    product.getProductName(),
                                    product.getPrice(),
                                    (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null
                            ));
                        });
            }

            // 11. 构造主分类 DTO
            categoryList.add(new RecommendCategoryResponseDto(
                    category.getCategoryId(),
                    category.getCategoryName(),
                    subListDto,   // 'children': sub_list
                    allProductsDto // 'products': all_products
            ));
        }

        return categoryList;
    }
}