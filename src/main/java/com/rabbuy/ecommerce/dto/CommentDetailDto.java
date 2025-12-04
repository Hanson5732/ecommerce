package com.rabbuy.ecommerce.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record CommentDetailDto(
        String id,
        String commentDesc,
        List<String> images,
        OffsetDateTime updatedTime,
        Double rating,
        OrderItemSnapshotDto orderItem
) {

    public record OrderItemSnapshotDto(
            String image,
            String name
    ) {}
}