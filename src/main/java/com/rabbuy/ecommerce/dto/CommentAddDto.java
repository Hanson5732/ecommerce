package com.rabbuy.ecommerce.dto;

import java.util.List;
import java.util.UUID;

// 对应 add_comment_view 的输入
public record CommentAddDto(
        String orderItemId,
        String productId,
        String commentDesc,
        Double rating,
        List<String> images
) {
}