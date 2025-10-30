package com.rabbuy.ecommerce.dto;

import java.util.List;

public record CommentUpdateDto(
        String commentDesc,
        Double rating,
        List<String> images
) {
}