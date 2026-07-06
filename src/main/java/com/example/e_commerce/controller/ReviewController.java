package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.CreateReviewReq;
import com.example.e_commerce.dto.response.ReviewRes;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.mapper.ReviewMapper;
import com.example.e_commerce.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    @PostMapping
    @PreAuthorize("hasAuthority('REVIEW_CREATE')")
    public ResponseEntity<ReviewRes> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateReviewReq req
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewMapper.toReviewRes(reviewService.create(currentUser, req)));
    }

    @GetMapping("/products/{productId}")
    @PreAuthorize("hasAuthority('REVIEW_READ')")
    public Page<ReviewRes> findByProduct(
            @PathVariable Long productId,
            Pageable pageable
    ) {
        return reviewService.findByProduct(productId, pageable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REVIEW_DELETE')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        reviewService.delete(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
