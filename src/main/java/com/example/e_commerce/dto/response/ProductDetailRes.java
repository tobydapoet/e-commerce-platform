package com.example.e_commerce.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailRes(
        Long id,
        String name,
        String thumbnail,
        String description,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<AttributeRes> attributes
) {}
