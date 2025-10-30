package com.rabbuy.ecommerce.dto;

import java.util.UUID;

public record AddressDto(
        UUID id,
        String tag,
        String recipient,
        String phone,
        String province,
        String city,
        String district,
        String additionalAddr,
        String postalCode,
        boolean isDefault
) {
}