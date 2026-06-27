package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.LoginReq;
import com.example.e_commerce.dto.request.RegisterReq;
import com.example.e_commerce.dto.response.LoginRes;
import com.example.e_commerce.dto.response.MessageRes;
import com.example.e_commerce.dto.response.TokenRes;
import com.example.e_commerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginRes login(@RequestBody LoginReq loginReq) {
        return authService.login(loginReq);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageRes> register(@Valid @ModelAttribute RegisterReq registerReq) {
        authService.register(registerReq);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageRes("User registered successfully."));
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<MessageRes> logout(@PathVariable String id) {
        authService.logout(id);
        return ResponseEntity.ok(new MessageRes("Logout successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRes> refreshToken(
            @RequestBody String token) {
        String accessToken = authService.refreshToken(token);
        return ResponseEntity.ok(new TokenRes(accessToken));
    }
}
