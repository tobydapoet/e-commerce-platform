package com.example.e_commerce.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SepayWebhookReq {
    private Long id;
    private String gateway;
    private LocalDateTime transactionDate;
    private String accountNumber;
    private String subAccount;
    private String transferType;

    @JsonAlias({"transferAmount", "amount"})
    private BigDecimal transferAmount;

    private BigDecimal accumulated;
    private String code;
    private String content;

    @JsonAlias({"referenceCode", "transactionCode"})
    private String referenceCode;

    private String description;
}
