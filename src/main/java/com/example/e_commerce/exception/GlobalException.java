package com.example.e_commerce.exception;

import com.example.e_commerce.dto.response.ErrorRes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRes> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorRes(
                        "INVALID_REQUEST",
                        "Request body is required and must be valid JSON",
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRes> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorRes(
                        "VALIDATION_ERROR",
                        "Invalid input",
                        ex.getBindingResult().getFieldErrors()
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRes> handleNoResourceFound(
            NoResourceFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorRes(
                        "RESOURCE_NOT_FOUND",
                        "The requested resource does not exist",
                        null
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorRes(
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred",
                        null
                ));
    }
}