package com.rabbuy.ecommerce.entity;

import com.rabbuy.ecommerce.converter.ProductSnapshotConverter;
import com.rabbuy.ecommerce.dto.ProductSnapshot;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "order_orderitem")
public class OrderItem {

    @Id
    @Column(name = "item_id", nullable = false, length = 255)
    private String itemId;

    @Column(name = "item_status", nullable = false, length = 2, columnDefinition = "VARCHAR(2) DEFAULT '0'")
    private String itemStatus = "0";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product", nullable = false, columnDefinition = "json")
    private ProductSnapshot product;

    @Column(name = "is_read", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdTime;

    @Column(name = "updated_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime updatedTime;

    public OrderItem() {
    }

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
    public ProductSnapshot getProduct() { return product; }
    public void setProduct(ProductSnapshot product) { this.product = product; }
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
        return "OrderItem " + itemId + " - Status: " + itemStatus;
    }
}