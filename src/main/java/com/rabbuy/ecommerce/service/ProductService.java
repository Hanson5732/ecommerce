package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.*;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

public interface ProductService {
    ProductDetailDto getProductDetails(String productId) throws NotFoundException;
    List<ProductListDto> getLatestProducts(int limit);
    List<ProductListDto> getHotProducts(int limit);
    PaginatedResult<ProductListDto> searchProducts(ProductSearchCriteria criteria);
    List<ProductListDto> getProductRecommendations(String productId, String name, int limit);
    ProductStatusDto getProductStatus(String productId) throws NotFoundException;

    ProductStockStatus getProductStockStatus();
    PaginatedResult<ProductAdminListDto> getAdminProductList(String query, int page, int pageSize);
    ProductDetailDto getAdminProductDetails(String productId) throws NotFoundException;
    ProductDetailDto addProduct(ProductAdminInputDto dto) throws NotFoundException, IllegalArgumentException;
    ProductDetailDto updateProduct(String productId, ProductAdminInputDto dto) throws NotFoundException, IllegalArgumentException;
    void deleteProduct(String productId) throws NotFoundException;
    List<HomeProductResponseDto> getHomeProducts();
    List<RecommendCategoryResponseDto> getRecommendCategories();
}