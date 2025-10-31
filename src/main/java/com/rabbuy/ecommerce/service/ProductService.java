package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.*;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDetailDto getProductDetails(UUID productId) throws NotFoundException;
    List<ProductListDto> getLatestProducts(int limit);
    List<ProductListDto> getHotProducts(int limit);
    PaginatedResult<ProductListDto> searchProducts(ProductSearchCriteria criteria);
    List<ProductListDto> getProductRecommendations(UUID productId, String name, int limit);
    ProductStatusDto getProductStatus(UUID productId) throws NotFoundException;

    ProductStockStatus getProductStockStatus();
    PaginatedResult<ProductAdminListDto> getAdminProductList(String query, int page, int pageSize);
    ProductDetailDto getAdminProductDetails(UUID productId) throws NotFoundException;
    ProductDetailDto addProduct(ProductAdminInputDto dto) throws NotFoundException, IllegalArgumentException;
    ProductDetailDto updateProduct(UUID productId, ProductAdminInputDto dto) throws NotFoundException, IllegalArgumentException;
    void deleteProduct(UUID productId) throws NotFoundException;
}