package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Answer;
import java.util.Optional;
import java.util.UUID;

// Answer 数据访问对象接口
public interface AnswerDao {

    /**
     * 保存新回答
     */
    void save(Answer answer);

    /**
     * 根据 ID 查找回答
     */
    Optional<Answer> findById(UUID answerId);
}