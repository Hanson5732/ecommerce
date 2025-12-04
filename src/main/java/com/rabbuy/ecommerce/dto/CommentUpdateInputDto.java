package com.rabbuy.ecommerce.dto;

import java.util.List;

public record CommentUpdateInputDto(
        String id,
        String commentDesc,
        Double rating,
        List<String> images
) {
}