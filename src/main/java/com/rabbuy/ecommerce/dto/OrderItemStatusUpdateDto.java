package com.rabbuy.ecommerce.dto;


public record OrderItemStatusUpdateDto(
        String itemId, // item_id
        String oldStatus,
        String newStatus
) {
}