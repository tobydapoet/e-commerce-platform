package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePhoneReq {
    @NotBlank(message = "New phone number is required")
    @Pattern(regexp = "^\\+?\\d{9,15}$", message = "Invalid phone number format")
    private String newPhone;
}
