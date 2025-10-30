package com.rabbuy.ecommerce.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record QuestionResponseDto(
        UUID questionId,
        CommentUserDto user,
        String content,
        OffsetDateTime createdTime,
        List<AnswerResponseDto> answers,
        int answersCount
) {
}