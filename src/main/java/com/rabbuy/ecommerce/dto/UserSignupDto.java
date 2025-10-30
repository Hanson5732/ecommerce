package com.rabbuy.ecommerce.dto;

// 用于接收注册请求的数据
public record UserSignupDto(
        String username,
        String email,
        String firstName,
        String lastName,
        String password,
        String confirmPwd,
        Boolean isStaff
) {
}