package com.example.e_commerce.dto.response;

import com.example.e_commerce.constant.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderRes {
    private Long id;
    private String orderCode;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private String note;
    private List<OrderStoreRes> orderStores;
    private LocalDateTime createdAt;
}
