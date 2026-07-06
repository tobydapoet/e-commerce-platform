package com.example.e_commerce.controller;

import com.example.e_commerce.constant.ProductStatus;
import com.example.e_commerce.dto.request.CreateProductReq;
import com.example.e_commerce.dto.request.UpdateProductReq;
import com.example.e_commerce.dto.request.UpdateProductStatusReq;
import com.example.e_commerce.dto.response.MessageRes;
import com.example.e_commerce.dto.response.ProductDetailRes;
import com.example.e_commerce.dto.response.ProductRes;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.mapper.ProductMapper;
import com.example.e_commerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper mapper;

    @PostMapping
    public ResponseEntity<ProductRes> create(@Valid @ModelAttribute CreateProductReq request) {
        Product product = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toProductRes(product));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ProductDetailRes> findByIdWithPrice(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findByIdWithPrice(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductRes>> getByPage(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getByPage(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductRes>> search(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(productService.search(keyword, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageRes> update(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateProductReq request
    ) {
        productService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductStatusReq request
    ) {
        productService.updateStatus(id, request);
        return ResponseEntity.noContent().build();
    }
}