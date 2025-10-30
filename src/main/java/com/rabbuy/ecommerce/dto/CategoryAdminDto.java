package com.rabbuy.ecommerce.dto;

import java.util.List;
import java.util.UUID;

public record CategoryAdminDto(
        UUID id,
        String name,
        String status,
        List<String> imageURL
) {
}