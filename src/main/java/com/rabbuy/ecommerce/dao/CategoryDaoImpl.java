package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.entity.Category;
import com.rabbuy.ecommerce.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CategoryDaoImpl implements CategoryDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    @Transactional
    public void save(Category category) {
        if (category.getCategoryId() == null) {
            em.persist(category);
        } else {
            em.merge(category);
        }
    }

    @Override
    public Optional<Category> findById(String id) {
        Category category = em.find(Category.class, id);
        return Optional.ofNullable(category); // 返回 Optional 以优雅处理 null
    }

    @Override
    public PaginatedResult<Category> findAll(int page, int pageSize) {
        String jpql = "SELECT c FROM Category c ORDER BY c.categoryName ASC"; // 可以根据需要改为按创建时间排序
        String countJpql = "SELECT COUNT(c) FROM Category c";

        // 1. 查询数据
        TypedQuery<Category> query = em.createQuery(jpql, Category.class);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);
        List<Category> data = query.getResultList();

        // 2. 查询总数
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        long totalItems = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        return new PaginatedResult<>(data, totalItems, page, totalPages);
    }

    @Override
    public List<Category> findActiveCategories(int limit) {
        return em.createQuery("SELECT c FROM Category c WHERE c.status = '1'", Category.class)
                .setMaxResults(limit) // 限制结果数量
                .getResultList();
    }


    @Override
    @Transactional
    public void update(Category category) {
        // merge 方法会查找具有相同 ID 的实体，如果存在则更新，如果不存在则插入（取决于上下文）
        // 对于更新操作，确保传入的 category 对象是受管的或具有有效的 ID
        if (em.find(Category.class, category.getCategoryId()) != null) {
            em.merge(category);
        } else {
            throw new IllegalArgumentException("Category with id " + category.getCategoryId() + " not found for update.");
        }
    }

    @Override
    @Transactional
    public void delete(Category category) {
        // 需要确保实体是受管的（managed）才能删除
        if (em.contains(category)) {
            em.remove(category);
        } else {
            // 如果实体是分离的（detached），先 merge 再 remove
            Category managedCategory = em.merge(category);
            em.remove(managedCategory);
        }
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        // 查找实体，如果存在则调用 delete 方法
        findById(id).ifPresent(this::delete);
    }
}