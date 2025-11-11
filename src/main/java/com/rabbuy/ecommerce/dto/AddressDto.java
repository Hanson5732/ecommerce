package com.rabbuy.ecommerce.dto;


public record AddressDto(
        String id,
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