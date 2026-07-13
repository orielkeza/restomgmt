package com.restomgmt.site.cart.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

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

import com.restomgmt.site.cart.dto.AddToCartRequest;
import com.restomgmt.site.cart.dto.CartItemResponse;
import com.restomgmt.site.cart.dto.CartResponse;
import com.restomgmt.site.cart.dto.UpdateCartItemRequest;
import com.restomgmt.site.cart.services.CartService;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.util.JwtUtil;

@WebMvcTest(
    controllers = CartController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
})
@TestPropertySource(properties = {
    "spring.jpa.enabled=false"
})
@ActiveProfiles("uat")
@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @SpringBootConfiguration
    @Import(CartController.class)
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {

        // Set up security context so getCurrentUsername() works
        var auth = new UsernamePasswordAuthenticationToken(
            "testuser", null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        cartResponse = CartResponse.builder()
            .cartId(1L)
            .username("testuser")
            .items(List.of(
                CartItemResponse.builder()
                    .menuItemId(1L)
                    .itemName("Grilled Chicken")
                    .itemPrice(new BigDecimal("12.99"))
                    .quantity(2)
                    .subtotal(new BigDecimal("25.98"))
                    .build()
            ))
            .total(new BigDecimal("25.98"))
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCartShouldReturn200() throws Exception {
        when(cartService.getCart("testuser")).thenReturn(cartResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/cart"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(25.98))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].itemName")
                .value("Grilled Chicken"));
    }

    @Test
    void addItemShouldReturn200WhenValid() throws Exception {
        when(cartService.addItem(eq("testuser"), any(AddToCartRequest.class)))
            .thenReturn(cartResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuItemId\":1,\"quantity\":2}"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void addItemShouldReturn404WhenMenuItemNotFound() throws Exception {
        when(cartService.addItem(eq("testuser"), any(AddToCartRequest.class)))
            .thenThrow(new NoSuchElementException("Menu item not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuItemId\":99,\"quantity\":1}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void addItemShouldReturn400WhenItemUnavailable() throws Exception {
        when(cartService.addItem(eq("testuser"), any(AddToCartRequest.class)))
            .thenThrow(new IllegalStateException("Item not available"));

        mockMvc.perform(MockMvcRequestBuilders.post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuItemId\":1,\"quantity\":1}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void addItemShouldReturn400WhenQuantityIsZero() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuItemId\":1,\"quantity\":0}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void updateItemQuantityShouldReturn200WhenValid() throws Exception {
        when(cartService.updateItemQuantity(eq("testuser"), eq(1L),
                any(UpdateCartItemRequest.class)))
            .thenReturn(cartResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":3}"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void updateItemQuantityShouldReturn404WhenItemNotInCart() throws Exception {
        when(cartService.updateItemQuantity(eq("testuser"), eq(99L),
                any(UpdateCartItemRequest.class)))
            .thenThrow(new NoSuchElementException("Item not in cart"));

        mockMvc.perform(MockMvcRequestBuilders.put("/cart/items/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":3}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void removeItemShouldReturn200WhenValid() throws Exception {
        when(cartService.removeItem("testuser", 1L)).thenReturn(cartResponse);

        mockMvc.perform(MockMvcRequestBuilders.delete("/cart/items/1"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void removeItemShouldReturn404WhenItemNotInCart() throws Exception {
        when(cartService.removeItem("testuser", 99L))
            .thenThrow(new NoSuchElementException("Item not in cart"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/cart/items/99"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void clearCartShouldReturn204() throws Exception {
        doNothing().when(cartService).clearCart("testuser");

        mockMvc.perform(MockMvcRequestBuilders.delete("/cart"))
            .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}
