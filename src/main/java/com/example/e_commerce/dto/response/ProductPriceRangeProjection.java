package com.example.e_commerce.dto.response;


import java.math.BigDecimal;

public interface ProductPriceRangeProjection {
    Long getProductId();
    BigDecimal getMinPrice();
    BigDecimal getMaxPrice();
}
