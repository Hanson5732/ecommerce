package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Cart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped // CDI Bean
public class CartDaoImpl implements CartDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    public Optional<Cart> findByUserId(String userId) {
        //
        // Cart 与 User 是一对一关系
        String jpql = "SELECT c FROM Cart c WHERE c.user.id = :userId";
        try {
            return Optional.of(
                    em.createQuery(jpql, Cart.class)
                            .setParameter("userId", userId)
                            .getSingleResult() // 一对一关系，预期最多一个结果
            );
        } catch (NoResultException e) {
            return Optional.empty(); // 未找到该用户的购物车
        }
    }

    @Override
    public Optional<Cart> findById(String cartId) {
        return Optional.ofNullable(em.find(Cart.class, cartId));
    }

    @Override
    @Transactional
    public void save(Cart cart) {
        //
        em.persist(cart);
    }

    @Override
    @Transactional
    public Cart update(Cart cart) {
        //
        return em.merge(cart);
    }

    @Override
    @Transactional
    public void delete(Cart cart) {
        if (em.contains(cart)) {
            em.remove(cart);
        } else {
            Cart managedCart = em.merge(cart);
            em.remove(managedCart);
        }
    }
}