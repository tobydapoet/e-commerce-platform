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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service tests")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepo;
    @Mock private OrderRepository orderRepo;
    @Mock private ObjectProvider<PaymentService> paymentServiceProvider;

    private PaymentService paymentService;
    private Order order;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepo, orderRepo, paymentServiceProvider);

        order = new Order();
        order.setId(1L);
        order.setOrderCode("ORD20260706210000ABCD");
        order.setTotal(BigDecimal.valueOf(200_000));
    }

    @Nested
    @DisplayName("Confirm Paid")
    class ConfirmPaid {
        @DisplayName("Success creates paid payment")
        @Test
        void success_createsPaidPayment() {
            LocalDateTime paidAt = LocalDateTime.of(2026, 7, 6, 21, 0);

            when(paymentRepo.findByTransactionCode("TXN-1")).thenReturn(Optional.empty());
            when(orderRepo.findByOrderCode(order.getOrderCode())).thenReturn(Optional.of(order));
            when(paymentRepo.findByOrderId(order.getId())).thenReturn(Optional.empty());
            when(paymentRepo.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            Payment result = paymentService.confirmPaid(
                    order.getOrderCode(),
                    BigDecimal.valueOf(200_000),
                    "TXN-1",
                    paidAt
            );

            assertSame(order, result.getOrder());
            assertEquals(PaymentMethod.BANK_TRANSFER, result.getPaymentMethod());
            assertEquals(PaymentStatus.PAID, result.getPaymentStatus());
            assertEquals("TXN-1", result.getTransactionCode());
            assertEquals(0, BigDecimal.valueOf(200_000).compareTo(result.getAmount()));
            assertEquals(paidAt, result.getPaidAt());
            verify(paymentRepo).save(any(Payment.class));
        }
        @DisplayName("Existing transaction returns existing payment")
        @Test
        void existingTransaction_returnsExistingPayment() {
            Payment existing = new Payment();
            existing.setId(99L);

            when(paymentRepo.findByTransactionCode("TXN-1")).thenReturn(Optional.of(existing));

            Payment result = paymentService.confirmPaid(
                    order.getOrderCode(),
                    BigDecimal.valueOf(200_000),
                    "TXN-1",
                    null
            );

            assertSame(existing, result);
            verifyNoInteractions(orderRepo);
            verify(paymentRepo, never()).save(any());
        }
        @DisplayName("Existing order payment returns existing payment")
        @Test
        void existingOrderPayment_returnsExistingPayment() {
            Payment existing = new Payment();
            existing.setId(88L);

            when(paymentRepo.findByTransactionCode("TXN-2")).thenReturn(Optional.empty());
            when(orderRepo.findByOrderCode(order.getOrderCode())).thenReturn(Optional.of(order));
            when(paymentRepo.findByOrderId(order.getId())).thenReturn(Optional.of(existing));

            Payment result = paymentService.confirmPaid(
                    order.getOrderCode(),
                    BigDecimal.valueOf(200_000),
                    "TXN-2",
                    null
            );

            assertSame(existing, result);
            verify(paymentRepo, never()).save(any());
        }
        @DisplayName("Order not found throws resource not found")
        @Test
        void orderNotFound_throwsResourceNotFound() {
            when(paymentRepo.findByTransactionCode("TXN-1")).thenReturn(Optional.empty());
            when(orderRepo.findByOrderCode(order.getOrderCode())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> paymentService.confirmPaid(
                            order.getOrderCode(),
                            BigDecimal.valueOf(200_000),
                            "TXN-1",
                            null
                    ));

            verify(paymentRepo, never()).save(any());
        }
        @DisplayName("Paid amount less than total throws bad request")
        @Test
        void paidAmountLessThanTotal_throwsBadRequest() {
            when(paymentRepo.findByTransactionCode("TXN-1")).thenReturn(Optional.empty());
            when(orderRepo.findByOrderCode(order.getOrderCode())).thenReturn(Optional.of(order));
            when(paymentRepo.findByOrderId(order.getId())).thenReturn(Optional.empty());

            assertThrows(BadRequestException.class,
                    () -> paymentService.confirmPaid(
                            order.getOrderCode(),
                            BigDecimal.valueOf(199_999),
                            "TXN-1",
                            null
                    ));

            verify(paymentRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Confirm Sepay Webhook")
    class ConfirmSepayWebhook {
        @DisplayName("Incoming transfer extracts order code and creates payment")
        @Test
        void incomingTransfer_extractsOrderCodeAndCreatesPayment() {
            SepayWebhookReq req = new SepayWebhookReq();
            req.setId(123L);
            req.setTransferType("in");
            req.setTransferAmount(BigDecimal.valueOf(200_000));
            req.setContent("Thanh toan " + order.getOrderCode());
            req.setReferenceCode("SEPAY-TXN");

            when(paymentRepo.findByTransactionCode("SEPAY-TXN")).thenReturn(Optional.empty());
            when(orderRepo.findByOrderCode(order.getOrderCode())).thenReturn(Optional.of(order));
            when(paymentRepo.findByOrderId(order.getId())).thenReturn(Optional.empty());
            when(paymentRepo.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            Optional<Payment> result = paymentService.confirmSepayWebhook(req);

            assertTrue(result.isPresent());
            assertEquals("SEPAY-TXN", result.get().getTransactionCode());
        }
        @DisplayName("Outgoing transfer is ignored")
        @Test
        void outgoingTransfer_isIgnored() {
            SepayWebhookReq req = new SepayWebhookReq();
            req.setTransferType("out");
            req.setTransferAmount(BigDecimal.valueOf(200_000));
            req.setContent("Thanh toan " + order.getOrderCode());

            Optional<Payment> result = paymentService.confirmSepayWebhook(req);

            assertTrue(result.isEmpty());
            verifyNoInteractions(paymentRepo, orderRepo);
        }
    }
}
