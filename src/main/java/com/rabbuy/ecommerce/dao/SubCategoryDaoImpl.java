package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import jakarta.persistence.TypedQuery;
import com.rabbuy.ecommerce.entity.SubCategory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SubCategoryDaoImpl implements SubCategoryDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    public Optional<SubCategory> findById(String id) {
        return Optional.ofNullable(em.find(SubCategory.class, id));
    }

    @Override
    public List<SubCategory> findAll() {
        String jpql = "SELECT s FROM SubCategory s LEFT JOIN FETCH s.category";
        return em.createQuery(jpql, SubCategory.class).getResultList();
    }

    @Override
    public PaginatedResult<SubCategory> findAllPaginated(int page, int pageSize) {
        //
        String jpql = "SELECT s FROM SubCategory s LEFT JOIN FETCH s.category";
        String countJpql = "SELECT COUNT(s) FROM SubCategory s";

        TypedQuery<SubCategory> query = em.createQuery(jpql, SubCategory.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);

        long totalItems = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        List<SubCategory> data = query.getResultList();

        return new PaginatedResult<>(data, totalItems, page, totalPages);
    }

    @Override
    public long countByCategoryId(String categoryId) {
        //
        String jpql = "SELECT COUNT(s) FROM SubCategory s WHERE s.category.categoryId = :categoryId";
        return em.createQuery(jpql, Long.class)
                .setParameter("categoryId", categoryId)
                .getSingleResult();
    }

    @Override
    @Transactional
    public void save(SubCategory subCategory) {
        em.persist(subCategory);
    }

    @Override
    @Transactional
    public SubCategory update(SubCategory subCategory) {
        return em.merge(subCategory);
    }

    @Override
    @Transactional
    public void delete(SubCategory subCategory) {
        if (em.contains(subCategory)) {
            em.remove(subCategory);
        } else {
            SubCategory managed = em.merge(subCategory);
            em.remove(managed);
        }
    }

    @Override
    public List<SubCategory> findAllActive() {
        // 使用 JOIN FETCH 确保父分类被立即加载，避免 N+1 查询
        String jpql = "SELECT s FROM SubCategory s JOIN FETCH s.category WHERE s.status = '1'";
        return em.createQuery(jpql, SubCategory.class).getResultList();
    }

    @Override
    public List<SubCategory> findActiveByCategoryId(String categoryId) {
        String jpql = "SELECT s FROM SubCategory s WHERE s.category.categoryId = :categoryId AND s.status = '1'";
        return em.createQuery(jpql, SubCategory.class)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }

    @Override
    public List<SubCategory> findByCategoryId(String categoryId, int limit) {
        // 逻辑：根据分类ID查找启用状态的二级分类，并限制返回数量
        String jpql = "SELECT s FROM SubCategory s WHERE s.category.categoryId = :categoryId AND s.status = '1'";
        return em.createQuery(jpql, SubCategory.class)
                .setParameter("categoryId", categoryId)
                .setMaxResults(limit) // 在数据库层面限制数量
                .getResultList();
    }
}