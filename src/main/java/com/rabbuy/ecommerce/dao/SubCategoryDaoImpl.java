package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.SubCategory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SubCategoryDaoImpl implements SubCategoryDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    public long countByCategoryId(UUID categoryId) {
        //
        String jpql = "SELECT COUNT(s) FROM SubCategory s WHERE s.category.categoryId = :categoryId";
        return em.createQuery(jpql, Long.class)
                .setParameter("categoryId", categoryId)
                .getSingleResult();
    }
}