package com.example.e_commerce.dto.response;

public record InventoryRes(
        Long id,
        String sku,
        Integer reservedQuantity,
        String image
) {}
