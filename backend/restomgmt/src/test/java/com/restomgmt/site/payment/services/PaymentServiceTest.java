package com.restomgmt.site.payment.services;

import com.restomgmt.site.order.models.Order;
import com.restomgmt.site.order.models.OrderItem;
import com.restomgmt.site.order.models.OrderStatus;
import com.restomgmt.site.order.repositories.OrderRepository;
import com.restomgmt.site.menu.models.MenuItem;
import com.restomgmt.site.menu.models.Category;
import com.restomgmt.site.payment.config.MomoConfig;
import com.restomgmt.site.payment.dto.InitiatePaymentRequest;
import com.restomgmt.site.payment.dto.PaymentResponse;
import com.restomgmt.site.payment.models.Payment;
import com.restomgmt.site.payment.models.PaymentStatus;
import com.restomgmt.site.payment.repositories.PaymentRepository;
import com.restomgmt.site.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private MomoClient momoClient;
    @Mock private MomoConfig momoConfig;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .username("testuser")
            .email("test@test.com")
            .password("encoded")
            .enabled(true)
            .tokenExpired(false)
            .build();

        Category category = Category.builder().name("Mains").build();

        MenuItem menuItem = MenuItem.builder()
            .name("Grilled Chicken")
            .cost(new BigDecimal("12.99"))
            .available(true)
            .category(category)
            .build();
        ReflectionTestUtils.setField(menuItem, "id", 1L);

        OrderItem orderItem = OrderItem.builder()
            .menuItem(menuItem)
            .quantity(2)
            .priceAtOrder(new BigDecimal("12.99"))
            .build();

        order = Order.builder()
            .user(user)
            .status(OrderStatus.PENDING)
            .items(new ArrayList<>(List.of(orderItem)))
            .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        payment = Payment.builder()
            .order(order)
            .momoReferenceId("test-ref-id")
            .amount(new BigDecimal("25.98"))
            .payerPhone("+250788123456")
            .status(PaymentStatus.PENDING)
            .refundFlagged(false)
            .build();
        ReflectionTestUtils.setField(payment, "id", 1L);
    }

    // initiatePayment
    @Test
    void initiatePaymentShouldCreatePaymentWhenValid() {
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setPayerPhone("+250788123456");

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder_Id(1L)).thenReturn(Optional.empty());
        when(momoClient.getAccessToken()).thenReturn("test-token");
        when(momoConfig.getCurrency()).thenReturn("EUR");
        when(momoClient.requestToPay(any(), any(), any(), any(), any(), any()))
            .thenReturn("test-ref-id");
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse result = paymentService.initiatePayment(1L, request, "testuser");

        assertNotNull(result);
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals("test-ref-id", result.getMomoReferenceId());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void initiatePaymentShouldThrowWhenOrderNotFound() {
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setPayerPhone("+250788123456");

        when(orderRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> paymentService.initiatePayment(99L, request, "testuser"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void initiatePaymentShouldThrowWhenNotOrderOwner() {
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setPayerPhone("+250788123456");

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
            () -> paymentService.initiatePayment(1L, request, "otheruser"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void initiatePaymentShouldThrowWhenOrderNotPending() {
        order.setStatus(OrderStatus.CONFIRMED);

        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setPayerPhone("+250788123456");

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
            () -> paymentService.initiatePayment(1L, request, "testuser"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void initiatePaymentShouldThrowWhenPaymentAlreadyExists() {
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setPayerPhone("+250788123456");

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder_Id(1L)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class,
            () -> paymentService.initiatePayment(1L, request, "testuser"));
        verify(momoClient, never()).getAccessToken();
    }

    // checkAndUpdatePaymentStatus
    @Test
    void checkStatusShouldUpdateToSuccessfulAndConfirmOrder() {
        when(paymentRepository.findByOrder_Id(1L)).thenReturn(Optional.of(payment));
        when(momoClient.getAccessToken()).thenReturn("test-token");
        when(momoClient.checkPaymentStatus("test-token", "test-ref-id"))
            .thenReturn("SUCCESSFUL");
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse result = paymentService.checkAndUpdatePaymentStatus(1L);

        assertNotNull(result);
        verify(orderRepository).save(argThat(o ->
            o.getStatus() == OrderStatus.CONFIRMED));
        verify(paymentRepository).save(argThat(p ->
            p.getStatus() == PaymentStatus.SUCCESSFUL));
    }

    @Test
    void checkStatusShouldUpdateToFailedWhenMomoFails() {
        when(paymentRepository.findByOrder_Id(1L)).thenReturn(Optional.of(payment));
        when(momoClient.getAccessToken()).thenReturn("test-token");
        when(momoClient.checkPaymentStatus("test-token", "test-ref-id"))
            .thenReturn("FAILED");
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        paymentService.checkAndUpdatePaymentStatus(1L);

        verify(paymentRepository).save(argThat(p ->
            p.getStatus() == PaymentStatus.FAILED));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkStatusShouldNotUpdateWhenAlreadySuccessful() {
        payment.setStatus(PaymentStatus.SUCCESSFUL);

        when(paymentRepository.findByOrder_Id(1L)).thenReturn(Optional.of(payment));

        paymentService.checkAndUpdatePaymentStatus(1L);

        verify(momoClient, never()).getAccessToken();
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void checkStatusShouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findByOrder_Id(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> paymentService.checkAndUpdatePaymentStatus(99L));
    }

    // flagRefund
    @Test
    void flagRefundShouldFlagWhenPaymentIsSuccessful() {
        payment.setStatus(PaymentStatus.SUCCESSFUL);

        when(paymentRepository.findByOrder_Id(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse result = paymentService.flagRefund(1L);

        assertNotNull(result);
        verify(paymentRepository).save(argThat(Payment::isRefundFlagged));
    }

    @Test
    void flagRefundShouldThrowWhenPaymentNotSuccessful() {
        when(paymentRepository.findByOrder_Id(1L)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class,
            () -> paymentService.flagRefund(1L));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void flagRefundShouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findByOrder_Id(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> paymentService.flagRefund(99L));
    }

    // getPaymentByOrderId
    @Test
    void getPaymentByOrderIdShouldReturnPayment() {
        when(paymentRepository.findByOrder_Id(1L)).thenReturn(Optional.of(payment));

        PaymentResponse result = paymentService.getPaymentByOrderId(1L);

        assertNotNull(result);
        assertEquals("+250788123456", result.getPayerPhone());
    }

    @Test
    void getPaymentByOrderIdShouldThrowWhenNotFound() {
        when(paymentRepository.findByOrder_Id(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> paymentService.getPaymentByOrderId(99L));
    }
}