package com.rabbuy.ecommerce.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "address_address")
public class Address {

    @Id
    @GeneratedValue(generator = "uuid-hex")
    @GenericGenerator(name = "uuid-hex", strategy = "org.hibernate.id.UUIDHexGenerator")
    @Column(name = "address_id", updatable = false, nullable = false, columnDefinition = "CHAR(32)")
    private String addressId;

    @Column(name = "address_tag", nullable = false, length = 100)
    private String addressTag;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "province", length = 100) // 可空
    private String province;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "additional_address", nullable = false, length = 255)
    private String additionalAddress; // 详细地址

    @Column(name = "postal_code", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT '000000'")
    private String postalCode = "000000";

    @Column(name = "is_default", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDefault = false; // 是否为默认地址

    // JPA 需要无参构造函数
    public Address() {
    }

    // --- Getters and Setters ---
    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }
    public String getAddressTag() { return addressTag; }
    public void setAddressTag(String addressTag) { this.addressTag = addressTag; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAdditionalAddress() { return additionalAddress; }
    public void setAdditionalAddress(String additionalAddress) { this.additionalAddress = additionalAddress; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    @Override
    public String toString() {
        return recipientName + " - " + additionalAddress + ", " + district + ", " + city;
    }
}