package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.CreateAttributeValueReq;
import com.example.e_commerce.dto.request.UpdateAttributeValueReq;
import com.example.e_commerce.dto.response.AttributeValueRes;
import com.example.e_commerce.entity.AttributeValue;
import com.example.e_commerce.mapper.AttributeValueMapper;
import com.example.e_commerce.service.AttributeValueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attribute-values")
@RequiredArgsConstructor
public class AttributeValueController {
    private final AttributeValueService attributeValueService;
    private final AttributeValueMapper mapper;

    @PostMapping("/attributes/{attributeId}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_VALUE_CREATE')")
    public ResponseEntity<List<AttributeValueRes>> create(
            @PathVariable Long attributeId,
            @Valid @RequestBody CreateAttributeValueReq request
    ) {
        List<AttributeValue> attributeValues =
                attributeValueService.createForAttribute(attributeId, request.getValues());

        List<AttributeValueRes> response = attributeValues.stream()
                .map(mapper::toAttributeValueRes)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/attributes/{attributeId}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_VALUE_READ')")
    public ResponseEntity<List<AttributeValueRes>> findByAttributeId(
            @PathVariable Long attributeId
    ) {
        return ResponseEntity.ok(attributeValueService.findByAttributeId(attributeId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_VALUE_UPDATE')")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeValueReq request
    ) {
        attributeValueService.update(id, request.getValue());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_VALUE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attributeValueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
