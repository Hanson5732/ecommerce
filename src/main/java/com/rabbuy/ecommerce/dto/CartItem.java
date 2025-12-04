package com.rabbuy.ecommerce.dto;


public class CartItem {
    private String id;
    private int count;

    public CartItem() {
    }

    public CartItem(String id, int count) {
        this.id = id;
        this.count = count;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return count == cartItem.count && java.util.Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, count);
    }
}
