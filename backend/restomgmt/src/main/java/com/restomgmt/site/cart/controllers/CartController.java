package com.restomgmt.site.cart.controllers;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restomgmt.site.cart.dto.AddToCartRequest;
import com.restomgmt.site.cart.dto.CartResponse;
import com.restomgmt.site.cart.dto.UpdateCartItemRequest;
import com.restomgmt.site.cart.services.CartService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        String username = getCurrentUsername();
        return ResponseEntity.ok(cartService.getCart(username));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddToCartRequest request) {
        String username = getCurrentUsername();
        try {
            return ResponseEntity.ok(cartService.addItem(username, request));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/items/{menuItemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(@PathVariable Long menuItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        String username = getCurrentUsername();
        try {
            return ResponseEntity.ok(cartService.updateItemQuantity(
                username, menuItemId, request));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/items/{menuItemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long menuItemId) {
        String username = getCurrentUsername();
        try {
            return ResponseEntity.ok(cartService.removeItem(
                username, menuItemId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        String username = getCurrentUsername();
        cartService.clearCart(username);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
