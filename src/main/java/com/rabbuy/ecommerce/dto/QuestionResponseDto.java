package com.rabbuy.ecommerce.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record QuestionResponseDto(
        String questionId,
        CommentUserDto user,
        String content,
        OffsetDateTime createdTime,
        List<AnswerResponseDto> answers,
        int answersCount
) {
}