package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Category;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<Category> findById(UUID id) {
        Category category = em.find(Category.class, id);
        return Optional.ofNullable(category); // 返回 Optional 以优雅处理 null
    }

    @Override
    public List<Category> findAll() {
        // 使用 JPQL (Jakarta Persistence Query Language) 查询
        return em.createQuery("SELECT c FROM Category c", Category.class)
                .getResultList();
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
            // 可以选择抛出异常，因为通常期望更新已存在的实体
            throw new IllegalArgumentException("Category with id " + category.getCategoryId() + " not found for update.");
        }
        // 注意：如果 category 是从数据库加载后修改的受管实体，
        // 在事务提交时，JPA 会自动将更改同步到数据库，不一定需要显式调用 merge。
        // 但显式调用 merge 对于分离的（detached）实体是必要的。
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
    public void deleteById(UUID id) {
        findById(id).ifPresent(this::delete); // 查找实体，如果存在则调用 delete 方法
        // 或者直接执行删除查询
        // em.createQuery("DELETE FROM Category c WHERE c.categoryId = :id")
        //   .setParameter("id", id)
        //   .executeUpdate();
    }
}