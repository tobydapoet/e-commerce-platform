package com.example.e_commerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartItemRes {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String variantName;
    private String thumbnail;
    private List<AttributeValueRes> attributeValues;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer stock;
}
