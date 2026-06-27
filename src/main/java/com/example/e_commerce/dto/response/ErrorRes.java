package com.example.e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorRes {
    private String code;
    private String message;
    private Object errors;
}