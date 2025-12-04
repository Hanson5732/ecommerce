package com.rabbuy.ecommerce.dto;

import java.math.BigDecimal;
import java.util.UUID;

// POJO for the JSON snapshot stored in OrderItem.product
public class ProductSnapshot {
    private String id;
    private String name;
    private BigDecimal price;
    private String image; // 存储第一张图片的 URL
    private int count;

    // 构造函数
    public ProductSnapshot() {}

    public ProductSnapshot(String id, String name, BigDecimal price, String image, int count) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.count = count;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}