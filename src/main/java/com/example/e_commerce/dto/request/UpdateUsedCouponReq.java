package com.example.e_commerce.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUsedCouponReq {
    private Long quantity;
    private LocalDate endDate;
}
