package com.example.e_commerce.dto.request;

import com.example.e_commerce.constant.CreatorType;
import com.example.e_commerce.constant.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCouponReq {
    @NotBlank(message = "Coupon code is required")
    @Size(max = 20, message = "Coupon code must not exceed 20 characters")
    private String code;

    @NotNull(message = "Creator type is required")
    private CreatorType creatorType;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", inclusive = true,
            message = "Minimum order must be greater than or equal to 0")
    private BigDecimal minimumOrder;

    @DecimalMin(value = "0.0", inclusive = true,
            message = "Maximum discount must be greater than or equal to 0")
    private BigDecimal maximumDiscount;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Long quantity;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}
