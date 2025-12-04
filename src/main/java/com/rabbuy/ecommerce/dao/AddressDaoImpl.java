package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Address;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped // CDI Bean
public class AddressDaoImpl implements AddressDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    public Optional<Address> findById(String id) {
        return Optional.ofNullable(em.find(Address.class, id));
    }

    @Override
    public List<Address> findByUserId(String userId) {
        // 按 is_default 降序排列，确保默认地址在前
        String jpql = "SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC";
        return em.createQuery(jpql, Address.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public Optional<Address> findDefaultByUserId(UUID userId) {
        String jpql = "SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true";
        try {
            return Optional.of(
                    em.createQuery(jpql, Address.class)
                            .setParameter("userId", userId)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Address> findNonDefaultsByUserId(String userId) {
        String jpql = "SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = false";
        return em.createQuery(jpql, Address.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public long countByUserId(String userId) {
        String jpql = "SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId";
        return em.createQuery(jpql, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    @Transactional
    public void save(Address address) {
        // 假设 address 是新创建的，且 ID 是自动生成的或已设置
        em.persist(address);
    }

    @Override
    @Transactional
    public Address update(Address address) {
        // Merge 返回受管的实例
        return em.merge(address);
    }

    @Override
    @Transactional
    public void delete(Address address) {
        if (em.contains(address)) {
            em.remove(address);
        } else {
            Address managedAddress = em.merge(address);
            em.remove(managedAddress);
        }
    }
}