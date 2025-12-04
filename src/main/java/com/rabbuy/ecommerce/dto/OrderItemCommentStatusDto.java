package com.rabbuy.ecommerce.dto;


public record OrderItemCommentStatusDto(
        String orderItemId,
        ProductSnapshot product,
        boolean isCommented
) {
}