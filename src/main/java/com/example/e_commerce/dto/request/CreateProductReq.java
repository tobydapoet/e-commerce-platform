package com.example.e_commerce.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductReq {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private MultipartFile thumbnail;

    @NotEmpty
    @Valid
    private List<VariantReq> variants;

    @Data
    public static class VariantReq {

        @NotNull
        @DecimalMin("0")
        private BigDecimal price;

        @NotNull
        @Min(0)
        private Integer quantity;

        private MultipartFile image;

        @NotEmpty
        @Valid
        private List<OptionReq> options;
    }

    @Data
    public static class OptionReq {

        @NotBlank
        private String name;

        @NotBlank
        private String value;
    }
}
