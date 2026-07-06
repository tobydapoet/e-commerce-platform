package com.example.e_commerce.controller;

import com.example.e_commerce.dto.request.SepayWebhookReq;
import com.example.e_commerce.dto.response.MessageRes;
import com.example.e_commerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/sepay")
@RequiredArgsConstructor
public class SepayWebhookController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<MessageRes> handle(@RequestBody SepayWebhookReq req) {
        return paymentService.confirmSepayWebhook(req)
                .map(payment -> ResponseEntity.ok(new MessageRes("Payment confirmed.")))
                .orElseGet(() -> ResponseEntity.ok(new MessageRes("Payment ignored.")));
    }
}
