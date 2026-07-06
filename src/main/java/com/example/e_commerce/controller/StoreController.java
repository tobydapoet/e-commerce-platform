package com.example.e_commerce.controller;

import com.example.e_commerce.constant.StoreStatus;
import com.example.e_commerce.dto.request.CreateStoreReq;
import com.example.e_commerce.dto.request.UpdatePhoneReq;
import com.example.e_commerce.dto.request.UpdateStoreReq;
import com.example.e_commerce.dto.request.VerifyOtpReq;
import com.example.e_commerce.dto.response.MessageRes;
import com.example.e_commerce.dto.response.StoreRes;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('STORE_CREATE')")
    public ResponseEntity<MessageRes> create(
            @Valid @ModelAttribute CreateStoreReq req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        storeService.create(req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageRes("Store created store successfully."));
    }

    @PutMapping("/{storeId}")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ResponseEntity<MessageRes> update(
            @PathVariable Long storeId,
            @Valid @ModelAttribute UpdateStoreReq req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        storeService.update(storeId, req, currentUser);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageRes("Store updated successfully."));
    }

    @PostMapping("/{storeId}/phone/request-otp")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ResponseEntity<MessageRes> requestPhoneOtp(
            @PathVariable Long storeId,
            @Valid @RequestBody UpdatePhoneReq req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        storeService.requestPhoneUpdateOtp(storeId, req, currentUser);
        return ResponseEntity.ok(new MessageRes("OTP sent successfully."));
    }

    @PostMapping("/{storeId}/phone/verify-otp")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ResponseEntity<MessageRes> verifyPhoneOtp(
            @PathVariable Long storeId,
            @Valid @RequestBody VerifyOtpReq req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        storeService.verifyAndUpdatePhone(storeId, req.getOtp(), currentUser);
        return ResponseEntity.ok(new MessageRes("Store updated successfully."));
    }

    @PatchMapping("/{storeId}/status")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ResponseEntity<MessageRes> updateStatus(
            @PathVariable Long storeId,
            @RequestParam StoreStatus status
    ) {
        storeService.updateStatus(storeId, status);
        return ResponseEntity.ok(new MessageRes("Store status updated successfully."));
    }

    @PatchMapping("/{storeId}/deactivate")
    @PreAuthorize("hasAuthority('STORE_DELETE')")
    public ResponseEntity<MessageRes> deactivate(
            @PathVariable Long storeId,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        storeService.deactivate(storeId, currentUser);
        return ResponseEntity.ok(new MessageRes("Store deactivated successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STORE_READ')")
    public Page<StoreRes> search(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return storeService.search(keyword, pageable);
    }

    @GetMapping("/my-stores")
    @PreAuthorize("hasAuthority('STORE_READ')")
    public ResponseEntity<List<StoreRes>> getMyStores(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(storeService.getMyStores(currentUser));
    }
}
