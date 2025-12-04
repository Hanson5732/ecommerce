package com.rabbuy.ecommerce.dto;

import java.util.List;

public class OrderCreateDto {
    private String userId;
    private String addressId;
    private String deliveryTime;
    private List<CartItem> products;

    // 必须：无参构造函数（JSONB 需要）
    public OrderCreateDto() {
    }

    public OrderCreateDto(String userId, String addressId, String deliveryTime, List<CartItem> products) {
        this.userId = userId;
        this.addressId = addressId;
        this.deliveryTime = deliveryTime;
        this.products = products;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public List<CartItem> getProducts() { return products; }
    public void setProducts(List<CartItem> products) { this.products = products; }
}