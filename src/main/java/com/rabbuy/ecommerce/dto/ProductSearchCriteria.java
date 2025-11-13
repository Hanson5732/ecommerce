package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;

// 封装来自 SearchView 的搜索条件
public record ProductSearchCriteria(
        String keyword,     // q
        String categoryId,    // category
        BigDecimal minPrice,  // sortMin
        BigDecimal maxPrice,  // sortMax
        String sortField,   // sortField (e.g., 'default', 'created_time', 'price')
        int page,           // page
        int pageSize        // pageSize
) {
}