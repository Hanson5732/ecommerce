package com.rabbuy.ecommerce.dto;

public record UserResponseDto(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String profilePicture,
        boolean isStaff
) {
}