package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Answer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AnswerDaoImpl implements AnswerDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    @Transactional
    public void save(Answer answer) {
        //
        em.persist(answer);
    }

    @Override
    public Optional<Answer> findById(UUID answerId) {
        return Optional.ofNullable(em.find(Answer.class, answerId));
    }
}