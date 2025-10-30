package com.rabbuy.ecommerce.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AnswerResponseDto(
        UUID answerId,
        CommentUserDto user,
        String content,
        OffsetDateTime createdTime
) {
}