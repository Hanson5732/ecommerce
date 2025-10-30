package com.rabbuy.ecommerce.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String profilePicture,
        boolean isStaff
) {
}