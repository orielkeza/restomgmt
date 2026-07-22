package com.restomgmt.site.payment.controllers;

import com.restomgmt.site.payment.dto.InitiatePaymentRequest;
import com.restomgmt.site.payment.dto.PaymentResponse;
import com.restomgmt.site.payment.models.PaymentStatus;
import com.restomgmt.site.payment.services.PaymentService;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(
    controllers = PaymentController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
})
@TestPropertySource(properties = {
    "spring.jpa.enabled=false"
})
@ActiveProfiles("uat")
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @SpringBootConfiguration
    @Import(PaymentController.class)
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken(
            "testuser", null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        paymentResponse = PaymentResponse.builder()
            .paymentId(1L)
            .orderId(1L)
            .momoReferenceId("test-ref-id")
            .amount(new BigDecimal("25.98"))
            .payerPhone("+250788123456")
            .status(PaymentStatus.PENDING)
            .refundFlagged(false)
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void initiatePaymentShouldReturn200WhenValid() throws Exception {
        when(paymentService.initiatePayment(eq(1L), any(InitiatePaymentRequest.class),
                eq("testuser")))
            .thenReturn(paymentResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/payments/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payerPhone\":\"+250788123456\"}"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.momoReferenceId")
                .value("test-ref-id"));
    }

    @Test
    void initiatePaymentShouldReturn404WhenOrderNotFound() throws Exception {
        when(paymentService.initiatePayment(eq(99L), any(InitiatePaymentRequest.class),
                any()))
            .thenThrow(new NoSuchElementException("Order not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/payments/orders/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payerPhone\":\"+250788123456\"}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void initiatePaymentShouldReturn400WhenOrderNotPending() throws Exception {
        when(paymentService.initiatePayment(eq(1L), any(InitiatePaymentRequest.class),
                any()))
            .thenThrow(new IllegalStateException("Order not pending"));

        mockMvc.perform(MockMvcRequestBuilders.post("/payments/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payerPhone\":\"+250788123456\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void initiatePaymentShouldReturn400WhenPhoneInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/payments/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payerPhone\":\"invalid\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void checkPaymentStatusShouldReturn200() throws Exception {
        PaymentResponse successful = PaymentResponse.builder()
            .paymentId(1L)
            .orderId(1L)
            .momoReferenceId("test-ref-id")
            .amount(new BigDecimal("25.98"))
            .payerPhone("+250788123456")
            .status(PaymentStatus.SUCCESSFUL)
            .refundFlagged(false)
            .build();

        when(paymentService.checkAndUpdatePaymentStatus(1L)).thenReturn(successful);

        mockMvc.perform(MockMvcRequestBuilders.get("/payments/orders/1/status"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESSFUL"));
    }

    @Test
    void checkPaymentStatusShouldReturn404WhenNotFound() throws Exception {
        when(paymentService.checkAndUpdatePaymentStatus(99L))
            .thenThrow(new NoSuchElementException("Payment not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/payments/orders/99/status"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void getPaymentShouldReturn200() throws Exception {
        when(paymentService.getPaymentByOrderId(1L)).thenReturn(paymentResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/payments/orders/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.payerPhone")
                .value("+250788123456"));
    }

    @Test
    void flagRefundShouldReturn200WhenValid() throws Exception {
        PaymentResponse refunded = PaymentResponse.builder()
            .paymentId(1L)
            .orderId(1L)
            .momoReferenceId("test-ref-id")
            .amount(new BigDecimal("25.98"))
            .payerPhone("+250788123456")
            .status(PaymentStatus.SUCCESSFUL)
            .refundFlagged(true)
            .build();

        when(paymentService.flagRefund(1L)).thenReturn(refunded);

        mockMvc.perform(MockMvcRequestBuilders.post("/payments/orders/1/refund"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.refundFlagged").value(true));
    }

    @Test
    void flagRefundShouldReturn400WhenPaymentNotSuccessful() throws Exception {
        when(paymentService.flagRefund(1L))
            .thenThrow(new IllegalStateException("Payment not successful"));

        mockMvc.perform(MockMvcRequestBuilders.post("/payments/orders/1/refund"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}