package com.rabbuy.ecommerce.dto;

// 用于接收登录请求的数据
public record UserLoginDto(
        String username,
        String password
) {
}