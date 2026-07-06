package com.example.e_commerce.dto.response;

import java.math.BigDecimal;

public record ProductVariantRes(
        Long id,
        String sku,
        BigDecimal price,
        String image,
        Integer quantity
) {}
