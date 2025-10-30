package com.rabbuy.ecommerce.dto;


public record AuthResponseDto(
        UserResponseDto user,
        String accessToken,
        String refreshToken
) {
}