package com.example.e_commerce.controller;

import com.example.e_commerce.constant.CouponStatus;
import com.example.e_commerce.constant.CreatorType;
import com.example.e_commerce.dto.request.CreateCouponReq;
import com.example.e_commerce.dto.request.UpdateUnusedCouponReq;
import com.example.e_commerce.dto.request.UpdateUsedCouponReq;
import com.example.e_commerce.entity.Coupon;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<Coupon> create(
            @RequestBody CreateCouponReq req,
            @RequestParam CreatorType creatorType,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Coupon coupon = couponService.create(req, creatorType);
        return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Coupon> updateStatus(
            @PathVariable Long id,
            @RequestParam CouponStatus status,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Coupon coupon = couponService.updateStatus(currentUser, id, status);
        return ResponseEntity.status(HttpStatus.OK).body(coupon);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coupon> update(
            @PathVariable Long id,
            @RequestBody UpdateUnusedCouponReq req
    ) {
        Coupon coupon = couponService.update(id, req);
        return ResponseEntity.status(HttpStatus.OK).body(coupon);
    }

    @PutMapping("/{id}/used")
    public ResponseEntity<Coupon> updateUsedCoupon(
            @PathVariable Long id,
            @RequestBody UpdateUsedCouponReq req
    ) {
        Coupon coupon = couponService.updateUsedCoupon(id, req);
        return ResponseEntity.status(HttpStatus.OK).body(coupon);
    }
}