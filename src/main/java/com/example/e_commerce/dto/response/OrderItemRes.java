package com.example.e_commerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemRes {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String thumbnail;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}