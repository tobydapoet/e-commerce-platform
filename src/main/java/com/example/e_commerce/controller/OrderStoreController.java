package com.example.e_commerce.controller;

import com.example.e_commerce.constant.OrderStoreStatus;
import com.example.e_commerce.dto.response.OrderStoreRes;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.OrderStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/orders")
@RequiredArgsConstructor
public class OrderStoreController {
    private final OrderStoreService orderStoreService;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_STORE_READ')")
    public ResponseEntity<Page<OrderStoreRes>> getOrdersByStore(
            @PathVariable Long storeId,
            @AuthenticationPrincipal User currentUser,
            Pageable pageable
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(orderStoreService.getOrdersByStore(currentUser, storeId, pageable));
    }

    @PatchMapping("/{orderStoreId}/status")
    @PreAuthorize("hasAuthority('ORDER_STORE_UPDATE')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long orderStoreId,
            @RequestParam OrderStoreStatus status,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        orderStoreService.updateStatus(currentUser, orderStoreId, status);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
