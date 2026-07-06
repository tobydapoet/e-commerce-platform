package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.CreateAttributeReq;
import com.example.e_commerce.dto.request.UpdateAttributeReq;
import com.example.e_commerce.dto.response.AttributeRes;
import com.example.e_commerce.entity.Attribute;
import com.example.e_commerce.mapper.AttributeMapper;
import com.example.e_commerce.service.AttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attributes")
@RequiredArgsConstructor
public class AttributeController {
    private final AttributeService attributeService;
    private final AttributeMapper mapper;

    @PostMapping("/products/{productId}")
    public ResponseEntity<List<AttributeRes>> create(
            @PathVariable Long productId,
            @RequestBody CreateAttributeReq req
    ) {
        List<Attribute> attributes = attributeService.createForProduct(req, productId);

        List<AttributeRes> response = attributes.stream()
                .map(mapper::toAttributeRes)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttributeRes> findById(@PathVariable Long id) {
        Attribute attribute = attributeService.findById(id);
        return ResponseEntity.ok(mapper.toAttributeRes(attribute));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<List<AttributeRes>> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(attributeService.findByProductId(productId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody UpdateAttributeReq req
    ) {
        attributeService.update(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attributeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}