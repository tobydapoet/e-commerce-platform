package com.example.e_commerce.controller;

import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.constant.UserStatus;
import com.example.e_commerce.dto.request.UpdateUserReq;
import com.example.e_commerce.dto.response.MessageRes;
import com.example.e_commerce.dto.response.UserSimpleRes;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.mapper.UserMapper;
import com.example.e_commerce.service.UserService;
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

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper mapper;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER_SELF_READ')")
    public ResponseEntity<UserSimpleRes> getCurrentUser(
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findById(currentUser.getId());
        return ResponseEntity.ok(mapper.toUserSimpleRes(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<UserSimpleRes> getById(
            @PathVariable UUID id
    ) {
        User user = userService.findById(id);
        return ResponseEntity.ok(mapper.toUserSimpleRes(user));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('USER_SELF_UPDATE')")
    public ResponseEntity<MessageRes> updateCurrentUser(
            @Valid @ModelAttribute UpdateUserReq req,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.updateUser(currentUser.getId(), req);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageRes("User updated successfully."));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<MessageRes> updateStatus(
            @PathVariable UUID id,
            @RequestParam UserStatus status
    ) {
        userService.updateUserStatus(id, status);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageRes("User status updated successfully."));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('USER_READ')")
    public Page<UserSimpleRes> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoleType roleName,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.search(keyword, roleName, status, pageable);
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAuthority('USER_READ')")
    public Page<UserSimpleRes> customerSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.customerSearch(keyword, pageable);
    }
}
