package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.CreateCartItemReq;
import com.example.e_commerce.dto.request.UpdateQuantityReq;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.CartItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart-items")
@RequiredArgsConstructor
public class CartItemController {
    private final CartItemService cartItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateCartItemReq req
    ) {
        cartItemService.create(currentUser, req);
    }

    @PatchMapping("/{id}/quantity")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuantity(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuantityReq req
    ) {
        cartItemService.updateQuantity(currentUser, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id
    ) {
        cartItemService.delete(currentUser, id);
    }
}