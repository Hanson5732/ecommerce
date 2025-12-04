package com.rabbuy.ecommerce.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_order")
public class Order {

    @Id
    @GeneratedValue(generator = "uuid-hex")
    @GenericGenerator(name = "uuid-hex", strategy = "org.hibernate.id.UUIDHexGenerator")
    @Column(name = "order_id", updatable = false, nullable = false, columnDefinition = "CHAR(32)")
    private String orderId;

    // 订单状态: '0': Unpaid, '1': Paid, '2': Canceled
    @Column(name = "order_status", nullable = false, length = 1, columnDefinition = "VARCHAR(1) DEFAULT '0'")
    private String orderStatus = "0";

    // 配送时间: '0': Anytime, '1': Weekday, '2': Weekend
    @Column(name = "delivery_time", nullable = false, length = 1, columnDefinition = "VARCHAR(1) DEFAULT '0'")
    private String deliveryTime = "0";

    // 支付方式: '0': Wechat, '1': AliPay, ... 可空
    @Column(name = "pay_method", length = 1)
    private String payMethod;

    // 多对一关系：多个订单可以属于一个用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 多对一关系：多个订单可以送到一个地址
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    // 一对多关系：一个订单包含多个订单项
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // JPA 需要无参构造函数
    public Order() {
    }

    // --- Helper methods for managing OrderItems ---
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // --- Getters and Setters ---
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    @Override
    public String toString() {
        return "Order " + orderId + " - " + (user != null ? user.getUsername() : "null");
    }
}