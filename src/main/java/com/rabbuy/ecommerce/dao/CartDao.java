package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Cart;
import java.util.Optional;

// Cart 数据访问对象接口
public interface CartDao {

    /**
     * 根据用户 ID 查找购物车 (核心查询方法)
     *
     */
    Optional<Cart> findByUserId(String userId);

    /**
     * 根据购物车 ID 查找
     */
    Optional<Cart> findById(String cartId);

    /**
     * 保存新购物车 (例如，用户注册时)
     *
     */
    void save(Cart cart);

    /**
     * 更新购物车 (例如，更新 products 列表)
     *
     * @return 返回受管的 Cart 实例
     */
    Cart update(Cart cart);

    /**
     * 删除购物车 (例如，用户销户时)
     */
    void delete(Cart cart);
}