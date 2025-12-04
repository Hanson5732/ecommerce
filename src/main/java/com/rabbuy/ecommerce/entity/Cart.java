package com.rabbuy.ecommerce.entity;

import com.rabbuy.ecommerce.converter.CartItemListToJsonConverter;
import com.rabbuy.ecommerce.dto.CartItem;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.Convert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cart_cart")
public class Cart {

    @Id
    @GeneratedValue(generator = "uuid-hex")
    @GenericGenerator(name = "uuid-hex", strategy = "org.hibernate.id.UUIDHexGenerator")
    @Column(name = "cart_id", updatable = false, nullable = false, columnDefinition = "CHAR(32)")
    private String cartId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "products", nullable = false, columnDefinition = "json")
    private List<CartItem> products = new ArrayList<>();

    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdTime;

    @Column(name = "updated_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime updatedTime;

    // JPA 需要无参构造函数
    public Cart() {
    }

    // 生命周期回调
    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdTime == null) {
            createdTime = now;
        }
        updatedTime = now;
        if (products == null) products = new ArrayList<>(); // 确保不为 null
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = OffsetDateTime.now();
        if (products == null) products = new ArrayList<>(); // 确保不为 null
    }

    // --- Getters and Setters ---
    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // 更新 Getter/Setter 使用 List<CartItem>
    public List<CartItem> getProducts() { return products; }
    public void setProducts(List<CartItem> products) {
        this.products = (products == null) ? new ArrayList<>() : products; // 防止 null
    }

    public OffsetDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(OffsetDateTime createdTime) { this.createdTime = createdTime; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(OffsetDateTime updatedTime) { this.updatedTime = updatedTime; }

    @Override
    public String toString() {
        return "Cart for " + (user != null ? user.getUsername() : "null");
    }
}
