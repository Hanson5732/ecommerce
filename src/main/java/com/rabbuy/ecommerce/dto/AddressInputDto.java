package com.rabbuy.ecommerce.dto;

public record AddressInputDto(
        String tag,
        String recipient,
        String phone,
        String province,
        String city,
        String district,
        String additionalAddress,
        String postalCode,
        Boolean isDefault
) {
}