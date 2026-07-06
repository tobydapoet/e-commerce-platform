package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewReq {
    @NotNull(message = "orderItemId is required.")
    private Long orderItemId;

    @NotNull(message = "rating is required.")
    @Min(value = 1, message = "rating must be at least 1.")
    @Max(value = 5, message = "rating must be at most 5.")
    private Integer rating;

    private String comment;
}
