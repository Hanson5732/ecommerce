package com.rabbuy.ecommerce.dto;

// 用于接收更新个人资料请求的数据
public record UserProfileUpdateDto(
        String username,
        String email,
        String phone,
        String profilePicture
) {
}