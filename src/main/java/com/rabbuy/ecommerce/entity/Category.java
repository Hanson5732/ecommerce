package com.rabbuy.ecommerce.entity;

import com.rabbuy.ecommerce.converter.StringListToJsonConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "category_category")
public class Category {

    @Id
    @GeneratedValue(generator = "uuid-hex")
    @GenericGenerator(name = "uuid-hex", strategy = "org.hibernate.id.UUIDHexGenerator")
    @Column(name = "category_id", updatable = false, nullable = false, columnDefinition = "CHAR(32)")
    private String categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "status", nullable = false, length = 1, columnDefinition = "VARCHAR(1) DEFAULT '0'")
    private String status = "0"; // "0"-禁用, "1"-启用

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_images", columnDefinition = "json")
    private List<String> categoryImages = new ArrayList<>();

    // JPA 需要一个无参构造函数
    public Category() {
    }

    // Getters and Setters
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getCategoryImages() { return categoryImages; }
    public void setCategoryImages(List<String> categoryImages) {
        this.categoryImages = (categoryImages == null) ? new ArrayList<>() : categoryImages; // 防止 null
    }

    // 生命周期回调，确保列表不为 null
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        if (categoryImages == null) {
            categoryImages = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return categoryName;
    }
}