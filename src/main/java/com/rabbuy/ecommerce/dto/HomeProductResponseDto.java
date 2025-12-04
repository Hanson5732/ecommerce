package com.rabbuy.ecommerce.dto;

import java.util.List;

public record HomeProductResponseDto(
        String id,
        String name,
        String saleInfo,
        String picture,
        List<HomeProductGoodsDto> goods
) {}