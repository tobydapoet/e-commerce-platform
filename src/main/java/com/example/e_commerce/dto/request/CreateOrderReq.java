package com.example.e_commerce.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderReq {
    private Long addressId;
    private List<Long> cartItemIds;
    private Long platformCouponId;
}
