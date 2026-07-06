package com.example.e_commerce.dto.response;

import com.example.e_commerce.constant.OrderStoreStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderStoreRes {
    private Long id;
    private Long storeId;
    private String storeName;
    private List<OrderItemRes> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private String note;
    private OrderStoreStatus status;
    private LocalDateTime createdAt;
}
