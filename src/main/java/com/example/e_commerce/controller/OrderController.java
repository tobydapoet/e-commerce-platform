package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.CreateOrderReq;
import com.example.e_commerce.dto.response.OrderRes;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.mapper.OrderMapper;
import com.example.e_commerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderRes> create(
            @RequestBody CreateOrderReq req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var order = orderService.create(currentUser, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderMapper.toOrderRes(order));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<OrderRes>> getMyOrders(
            @AuthenticationPrincipal User currentUser,
            Pageable pageable
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(orderService.getMyOrders(currentUser, pageable));
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<OrderRes> getMyOrderDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(orderService.getMyOrderDetail(currentUser, id));
    }
}