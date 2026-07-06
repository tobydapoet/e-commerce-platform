package com.example.e_commerce.controller;

import com.example.e_commerce.dto.response.CartStoreRes;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.mapper.CartStoreMapper;
import com.example.e_commerce.service.CartStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart-stores")
@RequiredArgsConstructor
public class CartStoreController {
    private final CartStoreService cartStoreService;

    @DeleteMapping("/{id}/coupon")
    @PreAuthorize("hasAuthority('CART_UPDATE')")
    public ResponseEntity<CartStoreRes> removeCoupon(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var cartStore = cartStoreService.removeCoupon(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CartStoreMapper.toStoreRes(cartStore));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('CART_READ')")
    public ResponseEntity<Page<CartStoreRes>> findAllByUser(
            @AuthenticationPrincipal User currentUser,
            Pageable pageable
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(cartStoreService.findAllByUser(currentUser, pageable));
    }
}
