package com.rabbuy.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.rabbuy.ecommerce.converter.StringListToJsonConverter;
import java.util.ArrayList;
import java.util.List;
import com.rabbuy.ecommerce.converter.ProductDetailConverter;
import com.rabbuy.ecommerce.dto.ProductDetailItem;
import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.Convert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "product_product")
public class Product {

    @Id
    @GeneratedValue(generator = "uuid-hex")
    @GenericGenerator(name = "uuid-hex", strategy = "org.hibernate.id.UUIDHexGenerator")
    @Column(name = "product_id", updatable = false, nullable = false, columnDefinition = "CHAR(32)")
    private String productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Lob
    @Column(name = "product_desc", nullable = false, columnDefinition = "TEXT")
    private String productDesc;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images", nullable = false, columnDefinition = "json DEFAULT '[]'")
    private List<String> images = new ArrayList<>();

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product_details", nullable = false, columnDefinition = "json DEFAULT '[]'")
    private List<ProductDetailItem> productDetails = new ArrayList<>();

    @Column(name = "product_rating", nullable = false, columnDefinition = "DOUBLE PRECISION DEFAULT 0.0")
    private Double productRating = 0.0;

    @Column(name = "rating_num", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer ratingNum = 0;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT '0'") // "0"-Unavailable, "1"-Available
    private String status = "0";

    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdTime;

    @Column(name = "updated_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime updatedTime;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDeleted = false; // 逻辑删除标记

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdTime DESC") // 问题的列表也可能需要排序
    private List<Question> questions = new ArrayList<>();

    // 多对一关系：多个 Product 属于一个 SubCategory
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false) // 外键列
    private SubCategory subCategory; // 引用 SubCategory 实体

    // JPA 需要无参构造函数
    public Product() {
    }

    // --- Getters and Setters ---
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductDesc() { return productDesc; }
    public void setProductDesc(String productDesc) { this.productDesc = productDesc; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images == null ? new ArrayList<>() : images; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Integer getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    public List<ProductDetailItem> getProductDetails() { return productDetails; }
    public void setProductDetails(List<ProductDetailItem> productDetails) {
        this.productDetails = (productDetails == null) ? new ArrayList<>() : productDetails;
    }    public Double getProductRating() { return productRating; }
    public void setProductRating(Double productRating) { this.productRating = productRating; }
    public Integer getRatingNum() { return ratingNum; }
    public void setRatingNum(Integer ratingNum) { this.ratingNum = ratingNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(OffsetDateTime createdTime) { this.createdTime = createdTime; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(OffsetDateTime updatedTime) { this.updatedTime = updatedTime; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public SubCategory getSubCategory() { return subCategory; }
    public void setSubCategory(SubCategory subCategory) { this.subCategory = subCategory; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }


    // 生命周期回调
    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdTime == null) {
            createdTime = now;
        }
        updatedTime = now;
        // 确保列表字段不是 null，以防转换器出错
        if (images == null) images = new ArrayList<>();
        if (productDetails == null) productDetails = new ArrayList<>();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = OffsetDateTime.now();
        // 确保列表字段不是 null
        if (images == null) images = new ArrayList<>();
        if (productDetails == null) productDetails = new ArrayList<>();
    }

    @Override
    public String toString() {
        return productName;
    }
}