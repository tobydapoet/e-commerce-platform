package com.example.e_commerce.dto.request;

import com.example.e_commerce.constant.CreatorType;
import com.example.e_commerce.constant.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateUnusedCouponReq {
    private String code;
    private CreatorType creatorType;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrder;
    private BigDecimal maximumDiscount;
    private Long quantity;
    private LocalDate startDate;
    private LocalDate endDate;
}
