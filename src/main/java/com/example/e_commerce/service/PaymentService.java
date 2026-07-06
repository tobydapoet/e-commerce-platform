package com.example.e_commerce.service;

import com.example.e_commerce.constant.PaymentMethod;
import com.example.e_commerce.constant.PaymentStatus;
import com.example.e_commerce.dto.request.SepayWebhookReq;
import com.example.e_commerce.entity.Order;
import com.example.e_commerce.entity.Payment;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.OrderRepository;
import com.example.e_commerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("\\bORD\\d{14}[A-Z0-9]{4}\\b");
    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;

    @Transactional
    public Payment confirmPaid(String orderCode, BigDecimal amount, String transactionCode, LocalDateTime paidAt) {
        return confirmPaidInternal(orderCode, amount, transactionCode, paidAt);
    }

    @Transactional
    public Optional<Payment> confirmSepayWebhook(SepayWebhookReq req) {
        if (req.getTransferType() != null && !"in".equalsIgnoreCase(req.getTransferType())) {
            return Optional.empty();
        }

        String orderCode = resolveOrderCode(req);
        String transactionCode = resolveTransactionCode(req);
        return Optional.of(confirmPaidInternal(
                orderCode,
                req.getTransferAmount(),
                transactionCode,
                req.getTransactionDate()
        ));
    }

    private Payment confirmPaidInternal(String orderCode, BigDecimal amount, String transactionCode, LocalDateTime paidAt) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new BadRequestException("Order code is required.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Paid amount must be greater than zero.");
        }

        if (transactionCode != null && !transactionCode.isBlank()) {
            Optional<Payment> existingByTransaction = paymentRepo.findByTransactionCode(transactionCode);
            if (existingByTransaction.isPresent()) {
                return existingByTransaction.get();
            }
        }

        Order order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        Optional<Payment> existingByOrder = paymentRepo.findByOrderId(order.getId());
        if (existingByOrder.isPresent()) {
            return existingByOrder.get();
        }

        if (amount.compareTo(order.getTotal()) < 0) {
            throw new BadRequestException("Paid amount is less than order total.");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setTransactionCode(transactionCode);
        payment.setAmount(amount);
        payment.setPaidAt(paidAt != null ? paidAt : LocalDateTime.now());

        return paymentRepo.save(payment);
    }

    private String resolveOrderCode(SepayWebhookReq req) {
        if (looksLikeOrderCode(req.getCode())) {
            return req.getCode().trim();
        }

        String combinedText = String.join(" ",
                nullToBlank(req.getContent()),
                nullToBlank(req.getDescription())
        );
        Matcher matcher = ORDER_CODE_PATTERN.matcher(combinedText);
        if (matcher.find()) {
            return matcher.group();
        }

        throw new BadRequestException("Order code not found in payment content.");
    }

    private String resolveTransactionCode(SepayWebhookReq req) {
        if (req.getReferenceCode() != null && !req.getReferenceCode().isBlank()) {
            return req.getReferenceCode().trim();
        }
        return req.getId() != null ? "SEPAY-" + req.getId() : null;
    }

    private boolean looksLikeOrderCode(String value) {
        return value != null && ORDER_CODE_PATTERN.matcher(value.trim()).matches();
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
