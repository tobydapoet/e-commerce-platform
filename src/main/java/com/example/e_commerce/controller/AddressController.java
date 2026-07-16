package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.CreateAddressReq;
import com.example.e_commerce.dto.request.UpdateAddressReq;
import com.example.e_commerce.dto.response.AddressRes;
import com.example.e_commerce.dto.response.MessageRes;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADDRESS_READ_ALL')")
    public Page<AddressRes> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return addressService.findAll(pageable);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('ADDRESS_READ')")
    public Page<AddressRes> getByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return addressService.findByUserId(currentUser.getId(),pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADDRESS_READ')")
    public ResponseEntity<AddressRes> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(addressService.getById(currentUser.getId(), id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADDRESS_CREATE')")
    public ResponseEntity<MessageRes> create(
            @Valid @RequestBody CreateAddressReq req,
            @AuthenticationPrincipal User currentUser
    ) {
        addressService.create(req, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageRes("User created address successfully."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADDRESS_UPDATE')")
    public ResponseEntity<MessageRes> update(
            @Valid @RequestBody UpdateAddressReq req,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        addressService.update(currentUser.getId(),id,req);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageRes("Address updated successfully."));
    }

    @PutMapping("/{id}/default")
    @PreAuthorize("hasAuthority('ADDRESS_UPDATE')")
    public ResponseEntity<MessageRes> updateDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        addressService.updateDefault(currentUser, id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageRes("Address updated default successfully."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADDRESS_DELETE')")
    public ResponseEntity<MessageRes> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        addressService.delete(currentUser.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageRes("Address deleted successfully."));
    }
}
