package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.entity.Comment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CommentDaoImpl implements CommentDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    @Transactional
    public void save(Comment comment) {
        em.persist(comment);
    }

    @Override
    public Optional<Comment> findById(String commentId) {
        return Optional.ofNullable(em.find(Comment.class, commentId));
    }

    @Override
    @Transactional
    public Comment update(Comment comment) {
        return em.merge(comment);
    }

    @Override
    @Transactional
    public void delete(Comment comment) {
        if (em.contains(comment)) {
            em.remove(comment);
        } else {
            Comment managedComment = em.merge(comment);
            em.remove(managedComment);
        }
    }

    @Override
    public boolean existsByOrderItemId(String orderItemId) {
        // Comment ID 与 OrderItem ID 相同
        String jpql = "SELECT COUNT(c) FROM Comment c WHERE c.orderItem.itemId = :orderItemId";
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("orderItemId", orderItemId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public PaginatedResult<Comment> findByProductId(String productId, String ratingFilter, boolean hasImageOnly, int page, int pageSize) {
        //
        StringBuilder jpql = new StringBuilder(
                "SELECT c FROM Comment c " +
                        "LEFT JOIN FETCH c.orderItem oi " +  // 预加载 OrderItem
                        "LEFT JOIN FETCH oi.order o " +      // 预加载 Order
                        "LEFT JOIN FETCH o.user u " +        // 预加载 User
                        "WHERE c.product.productId = :productId"
        );
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(c) FROM Comment c WHERE c.product.productId = :productId");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("productId", productId);

        // 评分过滤
        if ("5".equals(ratingFilter)) {
            jpql.append(" AND c.rating >= 4");
            countJpql.append(" AND c.rating >= 4");
        } else if ("3".equals(ratingFilter)) {
            jpql.append(" AND c.rating = 3");
            countJpql.append(" AND c.rating = 3");
        } else if ("1".equals(ratingFilter)) {
            jpql.append(" AND c.rating <= 2");
            countJpql.append(" AND c.rating <= 2");
        }

        // 图片过滤
        if (hasImageOnly) {
            // 假设空列表存储为 '[]'
            jpql.append(" AND c.images IS NOT NULL AND c.images <> '[]'");
            countJpql.append(" AND c.images IS NOT NULL AND c.images <> '[]'");
        }

        jpql.append(" ORDER BY c.updatedTime DESC"); // 按更新时间倒序

        // 创建查询
        TypedQuery<Comment> query = em.createQuery(jpql.toString(), Comment.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

        // 设置参数
        parameters.forEach(query::setParameter);
        parameters.forEach(countQuery::setParameter);

        // 获取总数
        long totalItems = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // 设置分页
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        List<Comment> data = query.getResultList();

        return new PaginatedResult<>(data, totalItems, page, totalPages);
    }
}