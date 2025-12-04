package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.entity.Order;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class OrderDaoImpl implements OrderDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    @Transactional
    public void save(Order order) {
        // 假设 Order 实体中的 @OneToMany(cascade=CascadeType.ALL)
        // 这样持久化 Order 时会自动持久化其 'items' 列表中的 OrderItem
        em.persist(order);
    }

    @Override
    @Transactional
    public Order update(Order order) {
        return em.merge(order);
    }

    @Override
    public Optional<Order> findOrderWithItems(String orderId) {
        String jpql = "SELECT o FROM Order o " +
                "LEFT JOIN FETCH o.items " +
                "LEFT JOIN FETCH o.user " +
                "LEFT JOIN FETCH o.address " +
                "WHERE o.orderId = :orderId";

        try {
            return Optional.of(
                    em.createQuery(jpql, Order.class)
                            .setParameter("orderId", orderId)
                            .getSingleResult()
            );
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> findOrderIdsByUserId(String userId) {
        // 从 OrderItem 反向查询其 Order ID
        String jpql = "SELECT DISTINCT i.order.orderId FROM OrderItem i WHERE i.order.user.id = :userId";
        return em.createQuery(jpql)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<String> findOrderIdsByUserIdAndItemStatus(String userId, String itemStatus) {
        //
        String jpql = "SELECT DISTINCT i.order.orderId FROM OrderItem i WHERE i.order.user.id = :userId AND i.itemStatus = :itemStatus";
        return em.createQuery(jpql)
                .setParameter("userId", userId)
                .setParameter("itemStatus", itemStatus)
                .getResultList();
    }

    @Override
    public List<Order> findOrdersWithItemsByOrderIds(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new ArrayList<>();
        }
        //
        // 再次使用 JOIN FETCH 预加载 Items, User, 和 Address，避免 N+1 查询
        String jpql = "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.items " +
                "LEFT JOIN FETCH o.user " +
                "LEFT JOIN FETCH o.address " +
                "WHERE o.orderId IN :orderIds";
        return em.createQuery(jpql, Order.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
        // 注意：Django 视图中复杂的 "按组内最新时间排序" 和 "按组分页"
        // 最好在获取数据后，在服务层(Service Layer)的 Java 代码中实现。
    }

    @Override
    public PaginatedResult<Order> findAdminOrders(String query, int page, int pageSize) {
        // (get_all_orders_view)
        StringBuilder jpql = new StringBuilder("SELECT o FROM Order o WHERE o.orderStatus <> '0'"); // 排除未支付 '0'
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(o) FROM Order o WHERE o.orderStatus <> '0'");
        Map<String, Object> parameters = new HashMap<>();

        if (query != null && !query.trim().isEmpty()) {
            String likeQuery = "%" + query.toLowerCase() + "%";
            jpql.append(" AND (o.orderId LIKE :query OR o.user.username LIKE :query)");
            countJpql.append(" AND (o.orderId LIKE :query OR o.user.username LIKE :query)");
            parameters.put("query", likeQuery);
        }

        // Django 视图按 OrderItem 的 created_time 排序，
        // 这在 JPQL 中很复杂 (需要子查询)。我们先按 orderId 降序排序作为替代。
        jpql.append(" ORDER BY o.orderId DESC");

        TypedQuery<Order> dataQuery = em.createQuery(jpql.toString(), Order.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

        parameters.forEach(dataQuery::setParameter);
        parameters.forEach(countQuery::setParameter);

        long totalItems = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        dataQuery.setFirstResult((page - 1) * pageSize);
        dataQuery.setMaxResults(pageSize);

        List<Order> data = dataQuery.getResultList();

        return new PaginatedResult<>(data, totalItems, page, totalPages);
    }

    /**
     * 按订单项状态查找所有订单 ID (管理员)
     *
     * @param itemStatus
     * @return
     */
    @Override
    public List<String> findOrderIdsByItemStatus(String itemStatus) {
        // 我们需要查询 OrderItem 表，并按 itemStatus 过滤，
        // 然后返回不重复的 Order ID 列表
        String jpql = "SELECT DISTINCT i.order.orderId FROM OrderItem i WHERE i.itemStatus = :itemStatus";
        return em.createQuery(jpql)
                .setParameter("itemStatus", itemStatus)
                .getResultList();
    }

    /**
     * 查找所有订单 ID (管理员)
     *
     * @return
     */
    @Override
    public List<String> findAllOrderIds() {
        // 直接从 Order 表返回所有 ID
        String jpql = "SELECT o.orderId FROM Order o";
        return em.createQuery(jpql)
                .getResultList();
    }
}