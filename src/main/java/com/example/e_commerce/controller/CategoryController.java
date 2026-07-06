package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.CreateCategoryReq;
import com.example.e_commerce.dto.response.MessageRes;
import com.example.e_commerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    public ResponseEntity<MessageRes> create(@Valid @RequestBody CreateCategoryReq request) {
        categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageRes("Category created successfully."));
    }
}
