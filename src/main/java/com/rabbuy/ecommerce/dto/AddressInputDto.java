package com.rabbuy.ecommerce.dto;

public record AddressInputDto(
        String tag,
        String recipient,
        String phone,
        String province,
        String city,
        String district,
        String additionalAddr,
        String postalCode,
        Boolean isDefault
) {
}