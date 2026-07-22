package com.restomgmt.site.payment.services;

import com.restomgmt.site.order.models.Order;
import com.restomgmt.site.order.models.OrderStatus;
import com.restomgmt.site.order.repositories.OrderRepository;
import com.restomgmt.site.payment.config.MomoConfig;
import com.restomgmt.site.payment.dto.InitiatePaymentRequest;
import com.restomgmt.site.payment.dto.PaymentResponse;
import com.restomgmt.site.payment.models.Payment;
import com.restomgmt.site.payment.models.PaymentStatus;
import com.restomgmt.site.payment.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MomoClient momoClient;
    private final MomoConfig momoConfig;

    @Transactional
    public PaymentResponse initiatePayment(Long orderId,
                                           InitiatePaymentRequest request,
                                           String username) {
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new NoSuchElementException("Order not found"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new IllegalStateException("You do not own this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Payment can only be initiated for PENDING orders");
        }

        if (paymentRepository.findByOrder_Id(orderId).isPresent()) {
            throw new IllegalStateException(
                "Payment already initiated for this order");
        }

        String accessToken = momoClient.getAccessToken();
        String amount = order.getItems().stream()
            .map(i -> i.getPriceAtOrder()
                .multiply(java.math.BigDecimal.valueOf(i.getQuantity())))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
            .toPlainString();

        String referenceId = momoClient.requestToPay(
            accessToken,
            request.getPayerPhone(),
            amount,
            momoConfig.getCurrency(),
            orderId.toString(),
            "Payment for order #" + orderId
        );

        Payment payment = Payment.builder()
            .order(order)
            .momoReferenceId(referenceId)
            .amount(new java.math.BigDecimal(amount))
            .payerPhone(request.getPayerPhone())
            .status(PaymentStatus.PENDING)
            .refundFlagged(false)
            .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment initiated for order {} - ref: {}", orderId, referenceId);

        return toResponse(saved);
    }

    @Transactional
    public PaymentResponse checkAndUpdatePaymentStatus(Long orderId) {
        Payment payment = paymentRepository.findByOrder_Id(orderId)
            .orElseThrow(() -> new NoSuchElementException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return toResponse(payment);
        }

        String accessToken = momoClient.getAccessToken();
        String momoStatus = momoClient.checkPaymentStatus(
            accessToken, payment.getMomoReferenceId());

        switch (momoStatus) {
            case "SUCCESSFUL" -> {
                payment.setStatus(PaymentStatus.SUCCESSFUL);
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                log.info("Payment successful for order {}", orderId);
            }
            case "FAILED" -> {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment rejected by MTN");
                log.warn("Payment failed for order {}", orderId);
            }
            default -> log.debug("Payment still pending for order {}", orderId);
        }

        return toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse flagRefund(Long orderId) {
        Payment payment = paymentRepository.findByOrder_Id(orderId)
            .orElseThrow(() -> new NoSuchElementException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESSFUL) {
            throw new IllegalStateException("Can only refund successful payments");
        }

        payment.setRefundFlagged(true);
        log.info("Refund flagged for order {}", orderId);

        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrder_Id(orderId)
            .orElseThrow(() -> new NoSuchElementException("Payment not found"));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
            .paymentId(payment.getId())
            .orderId(payment.getOrder().getId())
            .momoReferenceId(payment.getMomoReferenceId())
            .amount(payment.getAmount())
            .payerPhone(payment.getPayerPhone())
            .status(payment.getStatus())
            .failureReason(payment.getFailureReason())
            .refundFlagged(payment.isRefundFlagged())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .build();
    }
}