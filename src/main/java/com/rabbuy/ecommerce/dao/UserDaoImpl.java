package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserDaoImpl implements UserDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    @Transactional
    public void save(User user) {
        if (user.getId() == null) {
            em.persist(user); // 新增
        } else {
            em.merge(user); // 更新
        }
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty(); // 没有找到用户
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    @Override
    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
    }

    @Override
    @Transactional
    public void update(User user) {
        if (user.getId() != null && em.find(User.class, user.getId()) != null) {
            em.merge(user);
        } else {
            throw new IllegalArgumentException("User with id " + user.getId() + " not found for update.");
        }
    }

    @Override
    @Transactional
    public void delete(User user) {
        if (em.contains(user)) {
            em.remove(user);
        } else {
            User managedUser = em.merge(user);
            em.remove(managedUser);
        }
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }
}