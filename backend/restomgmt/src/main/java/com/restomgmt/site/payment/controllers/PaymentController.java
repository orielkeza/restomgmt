package com.restomgmt.site.payment.controllers;

import com.restomgmt.site.payment.dto.InitiatePaymentRequest;
import com.restomgmt.site.payment.dto.PaymentResponse;
import com.restomgmt.site.payment.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @PathVariable Long orderId,
            @Valid @RequestBody InitiatePaymentRequest request) {
        try {
            return ResponseEntity.ok(
                paymentService.initiatePayment(orderId, request, getCurrentUsername()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/orders/{orderId}/status")
    public ResponseEntity<PaymentResponse> checkPaymentStatus(
            @PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(
                paymentService.checkAndUpdatePaymentStatus(orderId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/orders/{orderId}/refund")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<PaymentResponse> flagRefund(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(paymentService.flagRefund(orderId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}