package com.restomgmt.site.order.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
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

import com.restomgmt.site.order.dto.AssignRiderRequest;
import com.restomgmt.site.order.dto.OrderItemResponse;
import com.restomgmt.site.order.dto.OrderResponse;
import com.restomgmt.site.order.dto.UpdateOrderStatusRequest;
import com.restomgmt.site.order.models.OrderStatus;
import com.restomgmt.site.order.services.OrderService;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.util.JwtUtil;

@WebMvcTest(
    controllers = OrderController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
    })
@TestPropertySource(properties = {
    "spring.jpa.enabled=false"
}) //it's needed for the repos but here it pulls it into a web-slice test and since the controller test doesn't have a db, it's looking for things that are not there
@ActiveProfiles("uat")
class OrderControllerTest {

    @SpringBootConfiguration
    @Import(OrderController.class)
    static class TestConfig {
    }
 
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken(
            "testuser", null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        orderResponse = OrderResponse.builder()
            .orderId(1L)
            .username("testuser")
            .status(OrderStatus.PENDING)
            .items(List.of(
                OrderItemResponse.builder()
                    .menuItemId(1L)
                    .itemName("Grilled Chicken")
                    .priceAtOrder(new BigDecimal("12.99"))
                    .quantity(2)
                    .subtotal(new BigDecimal("25.98"))
                    .build()
            ))
            .total(new BigDecimal("25.98"))
            .warnings(List.of())
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // placeOrder
    @Test
    void placeOrderShouldReturn201WhenValid() throws Exception {
        when(orderService.placeOrder("testuser")).thenReturn(orderResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/orders"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(25.98));
    }

    @Test
    void placeOrderShouldReturn400WhenCartIsEmpty() throws Exception {
        when(orderService.placeOrder("testuser"))
            .thenThrow(new IllegalStateException("Cannot place order with empty cart"));

        mockMvc.perform(MockMvcRequestBuilders.post("/orders"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void placeOrderShouldReturn404WhenCartNotFound() throws Exception {
        when(orderService.placeOrder("testuser"))
            .thenThrow(new NoSuchElementException("Cart not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/orders"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // getUserOrders
    @Test
    void getUserOrdersShouldReturn200() throws Exception {
        when(orderService.getUserOrders("testuser")).thenReturn(List.of(orderResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("testuser"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getUserOrdersShouldReturnEmptyList() throws Exception {
        when(orderService.getUserOrders("testuser")).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    // getOrderById
    @Test
    void getOrderByIdShouldReturn200WhenOwner() throws Exception {
        when(orderService.getOrderById("testuser", 1L)).thenReturn(orderResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(1));
    }

    @Test
    void getOrderByIdShouldReturn404WhenNotFound() throws Exception {
        when(orderService.getOrderById("testuser", 99L))
            .thenThrow(new NoSuchElementException("Order not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/99"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void getOrderByIdShouldReturn403WhenNotOwner() throws Exception {
        when(orderService.getOrderById("testuser", 1L))
            .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/1"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    // cancelOrder
    @Test
    void cancelOrderShouldReturn200WhenValid() throws Exception {
        OrderResponse cancelled = OrderResponse.builder()
            .orderId(1L)
            .username("testuser")
            .status(OrderStatus.CANCELLED)
            .items(List.of())
            .total(BigDecimal.ZERO)
            .warnings(List.of())
            .build();

        when(orderService.cancelOrder("testuser", 1L)).thenReturn(cancelled);

        mockMvc.perform(MockMvcRequestBuilders.delete("/orders/1/cancel"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrderShouldReturn400WhenWindowPassed() throws Exception {
        when(orderService.cancelOrder("testuser", 1L))
            .thenThrow(new IllegalStateException("Cancellation window has passed"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/orders/1/cancel"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void cancelOrderShouldReturn403WhenNotOwner() throws Exception {
        when(orderService.cancelOrder("testuser", 1L))
            .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/orders/1/cancel"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    // getAllOrders
    @Test
    void getAllOrdersShouldReturn200() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(orderResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/all"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].orderId").value(1));
    }

    // advanceOrderStatus
    @Test
    void advanceOrderStatusShouldReturn200WhenValid() throws Exception {
        OrderResponse confirmed = OrderResponse.builder()
            .orderId(1L)
            .username("testuser")
            .status(OrderStatus.CONFIRMED)
            .items(List.of())
            .total(BigDecimal.ZERO)
            .warnings(List.of())
            .build();

        when(orderService.advanceOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class)))
            .thenReturn(confirmed);

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CONFIRMED\"}"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void advanceOrderStatusShouldReturn400WhenInvalidTransition() throws Exception {
        when(orderService.advanceOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class)))
            .thenThrow(new IllegalStateException("Invalid status transition"));

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DELIVERED\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void advanceOrderStatusShouldReturn404WhenOrderNotFound() throws Exception {
        when(orderService.advanceOrderStatus(eq(99L), any(UpdateOrderStatusRequest.class)))
            .thenThrow(new NoSuchElementException("Order not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CONFIRMED\"}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void assignRiderShouldReturn200WhenValid() throws Exception {
        OrderResponse withRider = OrderResponse.builder()
            .orderId(1L)
            .username("testuser")
            .status(OrderStatus.OUTFORDELIVERY)
            .items(List.of())
            .total(BigDecimal.ZERO)
            .warnings(List.of())
            .riderPhone("+250788123456")
            .deliveryNote("Call on arrival")
            .build();

        when(orderService.assignRider(eq(1L), any(AssignRiderRequest.class)))
            .thenReturn(withRider);

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1/rider")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"riderPhone\":\"+250788123456\",\"deliveryNote\":\"Call on arrival\"}"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.riderPhone").value("+250788123456"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OUTFORDELIVERY"));
    }

    @Test
    void assignRiderShouldReturn400WhenOrderNotOutForDelivery() throws Exception {
        when(orderService.assignRider(eq(1L), any(AssignRiderRequest.class)))
            .thenThrow(new IllegalStateException("Order not out for delivery"));

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1/rider")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"riderPhone\":\"+250788123456\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void assignRiderShouldReturn400WhenPhoneIsInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/orders/1/rider")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"riderPhone\":\"invalid\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void assignRiderShouldReturn404WhenOrderNotFound() throws Exception {
        when(orderService.assignRider(eq(99L), any(AssignRiderRequest.class)))
            .thenThrow(new NoSuchElementException("Order not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/99/rider")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"riderPhone\":\"+250788123456\"}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
}
