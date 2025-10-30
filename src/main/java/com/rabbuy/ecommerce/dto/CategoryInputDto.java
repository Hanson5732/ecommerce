package com.rabbuy.ecommerce.dto;

import java.util.List;

// 这个 DTO 用于接收来自 API 请求的 JSON 数据
public record CategoryInputDto(
        String name,
        String status,
        List<String> imageURL
) {
}