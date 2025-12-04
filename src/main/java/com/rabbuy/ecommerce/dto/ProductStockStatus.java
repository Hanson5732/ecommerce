package com.rabbuy.ecommerce.dto;

// 封装来自 get_product_inventory_status_view 的结果
public record ProductStockStatus(
        long outOfStock, // stock_quantity = 0
        long lowStock    // 0 < stock_quantity < low_stock_threshold
) {
}