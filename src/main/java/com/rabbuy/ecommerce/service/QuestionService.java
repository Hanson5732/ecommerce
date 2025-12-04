package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.AnswerAddDto;
import com.rabbuy.ecommerce.dto.AnswerResponseDto;
import com.rabbuy.ecommerce.dto.QuestionAddDto;
import com.rabbuy.ecommerce.dto.QuestionResponseDto;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface QuestionService {

    /**
     * 获取产品的所有问题及回答
     *
     */
    List<QuestionResponseDto> getQuestionsByProductId(String productId);

    /**
     * 添加新问题
     *
     */
    QuestionResponseDto addQuestion(String productId, String userId, QuestionAddDto dto) throws NotFoundException;

    /**
     * 添加新回答
     *
     */
    AnswerResponseDto addAnswer(String questionId, String userId, AnswerAddDto dto) throws NotFoundException;
}