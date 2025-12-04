package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Question;
import java.util.List;
import java.util.Optional;

// Question 数据访问对象接口
public interface QuestionDao {

    /**
     * 保存新问题
     */
    void save(Question question);

    /**
     * 根据 ID 查找问题
     */
    Optional<Question> findById(String questionId);

    /**
     * 查找特定产品的所有问题（并预加载回答和用户信息）
     */
    List<Question> findByProductId(String productId);
}