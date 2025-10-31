package com.rabbuy.ecommerce.dto;

import com.rabbuy.ecommerce.entity.Product;
import java.util.UUID;


public record ProductStatusDto(
        UUID id,
        boolean status // true (可用) 或 false (不可用)
) {

    public static ProductStatusDto fromEntity(Product product) {
        //
        boolean isActive = "1".equals(product.getStatus()) && !product.isDeleted();
        return new ProductStatusDto(
                product.getProductId(),
                isActive
        );
    }
}