package com.rabbuy.ecommerce.dto;


public record HomeMessageCountDto(
        long unpaid,
        long pending,
        long review,
        long refunding
) {
}