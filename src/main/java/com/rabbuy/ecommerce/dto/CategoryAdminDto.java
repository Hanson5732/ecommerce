package com.rabbuy.ecommerce.dto;

import java.util.List;

public record CategoryAdminDto(
        String id,
        String name,
        String status,
        List<String> imageURL
) {
}