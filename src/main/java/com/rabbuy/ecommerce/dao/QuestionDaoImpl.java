package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Question;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class QuestionDaoImpl implements QuestionDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Override
    @Transactional
    public void save(Question question) {
        em.persist(question);
    }

    @Override
    public Optional<Question> findById(String questionId) {
        return Optional.ofNullable(em.find(Question.class, questionId));
    }

    @Override
    public List<Question> findByProductId(String productId) {
        //
        // 预加载 Answers, Question.User, Answer.User
        // 使用 LEFT JOIN FETCH 避免在没有答案时返回空
        String jpql = "SELECT DISTINCT q FROM Question q " +
                "LEFT JOIN FETCH q.answers a " + // 预加载答案
                "LEFT JOIN FETCH q.user qu " +  // 预加载提问者
                "LEFT JOIN FETCH a.user au " +  // 预加载回答者
                "WHERE q.product.productId = :productId " +
                "ORDER BY q.createdTime DESC, a.createdTime DESC"; // 按问题和答案时间排序

        return em.createQuery(jpql, Question.class)
                .setParameter("productId", productId)
                .getResultList();
    }
}