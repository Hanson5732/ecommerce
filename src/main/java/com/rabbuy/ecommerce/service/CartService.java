package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.CartItem;
import com.rabbuy.ecommerce.dto.CartResponseDto;
import com.rabbuy.ecommerce.entity.User;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface CartService {

    /**
     * 获取用户购物车详情（包含实时商品信息）
     *
     */
    CartResponseDto getCartByUserId(String userId) throws NotFoundException;

    /**
     * 完全覆盖用户的购物车
     *
     */
    void saveCart(String userId, List<CartItem> itemsDto) throws NotFoundException;

    /**
     * 创建空购物车 (用于 UserService)
     */
    void createEmptyCart(User user);
}