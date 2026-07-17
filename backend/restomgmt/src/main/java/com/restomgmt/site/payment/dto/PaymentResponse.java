package com.restomgmt.site.payment.dto;

import com.restomgmt.site.payment.models.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String momoReferenceId;
    private BigDecimal amount;
    private String payerPhone;
    private PaymentStatus status;
    private String failureReason;
    private boolean refundFlagged;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}