package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.OrderItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrderItemDaoImpl implements OrderItemDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    public Optional<OrderItem> findById(String itemId) {
        // (get_order_item_view)
        return Optional.ofNullable(em.find(OrderItem.class, itemId));
    }

    @Override
    @Transactional
    public OrderItem update(OrderItem item) {
        // (update_order_item_view, update_admin_item_status_view)
        return em.merge(item);
    }

    @Override
    public long countUnreadByUserId(String userId) {
        // (get_order_notification_view)
        // 通过 i.order.user.id 级联查询
        String jpql = "SELECT COUNT(i) FROM OrderItem i WHERE i.order.user.id = :userId AND i.isRead = false";
        return em.createQuery(jpql, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    @Transactional
    public int markAllAsReadByUserId(String userId) {
        // (update_order_notification_view)
        // 使用 JPQL UPDATE 批量更新
        String jpql = "UPDATE OrderItem i SET i.isRead = true " +
                "WHERE i.isRead = false AND i.order.id IN " +
                "(SELECT o.id FROM Order o WHERE o.user.id = :userId)";
        return em.createQuery(jpql)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public long countByUserIdAndItemStatus(String userId, String itemStatus) {
        String jpql = "SELECT COUNT(i) FROM OrderItem i WHERE i.order.user.id = :userId AND i.itemStatus = :itemStatus";
        return em.createQuery(jpql, Long.class)
                .setParameter("userId", userId)
                .setParameter("itemStatus", itemStatus)
                .getSingleResult();
    }
}