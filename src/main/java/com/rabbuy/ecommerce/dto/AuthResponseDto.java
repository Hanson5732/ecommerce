package com.rabbuy.ecommerce.dto;


public record AuthResponseDto(
        UserResponseDto user,
        String access,
        String refresh
) {
}