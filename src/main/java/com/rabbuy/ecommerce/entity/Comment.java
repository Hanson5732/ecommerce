package com.rabbuy.ecommerce.entity;

import com.rabbuy.ecommerce.converter.StringListToJsonConverter;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "comment_comment")
public class Comment {

    @Id
    @Column(name = "comment_id", nullable = false, length = 255)
    private String commentId;

    @Column(name = "rating", nullable = false, columnDefinition = "DOUBLE PRECISION DEFAULT 0.0")
    private Double rating = 0.0;

    @Lob // 映射为 TEXT
    @Column(name = "comment_desc", nullable = false, columnDefinition = "TEXT DEFAULT 'The user did not say anything'")
    private String commentDesc = "The user didn't say anything";

    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdTime;

    @Column(name = "updated_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime updatedTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images", columnDefinition = "json")
    private List<String> images = new ArrayList<>();

    // 多对一关系：多个评论可以属于一个产品
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 一对一关系：一个评论对应一个订单项
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    // JPA 需要无参构造函数
    public Comment() {
    }

    // 生命周期回调
    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdTime == null) {
            createdTime = now;
        }
        updatedTime = now;
        if (images == null) images = new ArrayList<>();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = OffsetDateTime.now();
        if (images == null) images = new ArrayList<>();
    }

    // --- Getters and Setters ---
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getCommentDesc() { return commentDesc; }
    public void setCommentDesc(String commentDesc) { this.commentDesc = commentDesc; }
    public OffsetDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(OffsetDateTime createdTime) { this.createdTime = createdTime; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(OffsetDateTime updatedTime) { this.updatedTime = updatedTime; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = (images == null) ? new ArrayList<>() : images; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public OrderItem getOrderItem() { return orderItem; }
    public void setOrderItem(OrderItem orderItem) { this.orderItem = orderItem; }

    @Override
    public String toString() {
        return "Comment " + commentId + ": " + commentDesc.substring(0, Math.min(commentDesc.length(), 30));
    }
}