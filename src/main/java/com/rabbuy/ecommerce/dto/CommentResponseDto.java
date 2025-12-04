package com.rabbuy.ecommerce.dto;

import java.time.OffsetDateTime;
import java.util.List;


public record CommentResponseDto(
        String id, // comment_id
        String commentDesc,
        Double rating,
        List<String> images,
        OffsetDateTime createdTime,
        CommentUserDto user
) {
}