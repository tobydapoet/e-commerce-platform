package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.UpdateQuantityReq;
import com.example.e_commerce.dto.response.InventoryRes;
import com.example.e_commerce.dto.response.MessageRes;
import com.example.e_commerce.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PatchMapping("/{id}/quantity")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public ResponseEntity<MessageRes> updateQuantity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuantityReq request
    ) {
        inventoryService.updateQuantity(id, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageRes("Inventory updated successfully."));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public Page<InventoryRes> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return inventoryService.search(keyword, pageable);
    }
}
