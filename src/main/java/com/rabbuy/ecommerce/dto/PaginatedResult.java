package com.rabbuy.ecommerce.dto;

import java.util.List;

// 一个通用的分页结果记录
public record PaginatedResult<T>(
        List<T> data,      // 当前页的数据
        long totalItems,   // 总记录数
        int currentPage,   // 当前页码
        int totalPages     // 总页数
) {
}