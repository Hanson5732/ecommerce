package com.rabbuy.ecommerce.dto;

// 一个标准的错误响应 DTO
public record ErrorResponseDto(
        int statusCode,
        String message
) {
}