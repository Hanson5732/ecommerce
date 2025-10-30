package com.rabbuy.ecommerce.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "category_subcategory")
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // 或 GenerationType.UUID
    @Column(name = "sub_cate_id", updatable = false, nullable = false)
    private UUID subCateId;

    @Column(name = "sub_cate_name", nullable = false, length = 100)
    private String subCateName;

    // 多对一关系：多个 SubCategory 对应一个 Category
    @ManyToOne(fetch = FetchType.LAZY) // LAZY 表示延迟加载关联的 Category
    @JoinColumn(name = "category_id", nullable = false) // 外键列名
    private Category category;

    @Column(name = "sub_cate_image", nullable = false, length = 255)
    private String subCateImage; // 存储图片 URL

    @Column(name = "status", nullable = false, length = 1, columnDefinition = "VARCHAR(1) DEFAULT '0'")
    private String status = "0"; // "0"-禁用, "1"-启用

    // 无参构造函数
    public SubCategory() {
    }

    // Getters and Setters
    public UUID getSubCateId() {
        return subCateId;
    }

    public void setSubCateId(UUID subCateId) {
        this.subCateId = subCateId;
    }

    public String getSubCateName() {
        return subCateName;
    }

    public void setSubCateName(String subCateName) {
        this.subCateName = subCateName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getSubCateImage() {
        return subCateImage;
    }

    public void setSubCateImage(String subCateImage) {
        this.subCateImage = subCateImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return subCateName;
    }
}