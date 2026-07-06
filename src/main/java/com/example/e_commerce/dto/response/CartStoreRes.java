package com.example.e_commerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CartStoreRes {
    private Long id;
    private Long storeId;
    private String storeName;
    private String storeLogo;

    private Long couponId;
    private String couponCode;

    private List<CartItemRes> items;

    private BigDecimal subtotal;

    private LocalDateTime createdAt;
}
