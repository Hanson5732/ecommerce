package com.rabbuy.ecommerce.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "order_orderitem")
public class OrderItem {

    @Id
    @Column(name = "item_id", nullable = false, length = 255) // 使用 Django 中生成的 String ID 作为主键
    private String itemId;

    // 订单项状态: '0':Unpaid, '1':Paid, ..., '9':Hold
    @Column(name = "item_status", nullable = false, length = 2, columnDefinition = "VARCHAR(2) DEFAULT '0'")
    private String itemStatus = "0";

    // 商品快照，存储为 JSON 字符串
    @Lob
    @Column(name = "product", nullable = false, columnDefinition = "TEXT") // 假设存储 JSON 的列类型是 TEXT
    private String product; // 存储商品快照的 JSON 字符串

    @Column(name = "is_read", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isRead = false; // 消息是否已读

    // 多对一关系：多个 OrderItem 属于一个 Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false) // 外键列
    private Order order;

    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdTime;

    @Column(name = "updated_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime updatedTime;

    // JPA 需要无参构造函数
    public OrderItem() {
    }

    // 生命周期回调
    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdTime == null) {
            createdTime = now;
        }
        updatedTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = OffsetDateTime.now();
    }

    // --- Getters and Setters ---
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemStatus() { return itemStatus; }
    public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; } // 可以添加 JSON 格式校验
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public OffsetDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(OffsetDateTime createdTime) { this.createdTime = createdTime; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(OffsetDateTime updatedTime) { this.updatedTime = updatedTime; }

    @Override
    public String toString() {
        // 避免在 toString 中加载 product JSON 带来的复杂性或性能问题
        return "OrderItem " + itemId + " - Status: " + itemStatus;
    }
}