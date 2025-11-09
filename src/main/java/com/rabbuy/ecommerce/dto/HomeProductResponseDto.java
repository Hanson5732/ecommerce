package com.rabbuy.ecommerce.dto;

import java.util.List;
import java.util.UUID;

public record HomeProductResponseDto(
        UUID id,
        String name,
        String saleInfo,
        String picture,
        List<HomeProductGoodsDto> goods
) {}