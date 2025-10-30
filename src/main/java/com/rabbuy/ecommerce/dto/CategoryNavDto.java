package com.rabbuy.ecommerce.dto;

import java.util.UUID;

public record CategoryNavDto(
        UUID id,
        String name
) {
}