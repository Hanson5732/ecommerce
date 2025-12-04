package com.rabbuy.ecommerce.dto;

import java.util.UUID;

/**
 * 用于创建和更新 SubCategory 的输入 DTO
 *
 */
public record SubCategoryInputDto(
        String name,
        String categoryId,
        String images, // Django 中 'images' 是 URL 字符串
        String status
) {
}