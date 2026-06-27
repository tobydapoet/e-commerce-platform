package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginGoogleReq {

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Avatar URL is required")
    private String avatarUrl;

    @NotBlank(message = "Google account ID is required")
    private String providedId;
}