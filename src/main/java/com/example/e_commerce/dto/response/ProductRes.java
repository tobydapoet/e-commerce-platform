package com.example.e_commerce.dto.response;

import com.example.e_commerce.constant.ProductStatus;

import java.math.BigDecimal;

public record ProductRes(
        Long id,
        String name,
        String thumbnail,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        ProductStatus status,
        Integer soldCount
) {}
