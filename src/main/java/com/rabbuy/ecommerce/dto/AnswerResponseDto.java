package com.rabbuy.ecommerce.dto;

import java.time.OffsetDateTime;

public record AnswerResponseDto(
        String answerId,
        CommentUserDto user,
        String content,
        OffsetDateTime createdTime
) {
}