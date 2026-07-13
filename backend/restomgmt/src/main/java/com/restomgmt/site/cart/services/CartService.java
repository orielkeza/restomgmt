package com.restomgmt.site.cart.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.restomgmt.site.cart.dto.AddToCartRequest;
import com.restomgmt.site.cart.dto.CartItemResponse;
import com.restomgmt.site.cart.dto.CartResponse;
import com.restomgmt.site.cart.dto.UpdateCartItemRequest;
import com.restomgmt.site.cart.models.Cart;
import com.restomgmt.site.cart.models.CartItem;
import com.restomgmt.site.cart.repositories.CartItemRepository;
import com.restomgmt.site.cart.repositories.CartRepository;
import com.restomgmt.site.menu.models.MenuItem;
import com.restomgmt.site.menu.repositories.MenuItemRepository;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    // Get or create cart for user
    private Cart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username).orElseGet(() -> {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public CartResponse getCart(String username) {
        Cart cart = getOrCreateCart(username);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String username, AddToCartRequest request) {
        Cart cart = getOrCreateCart(username);

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
            .orElseThrow(() -> new NoSuchElementException("Menu item not found"));

        if (!menuItem.isAvailable()) {
            throw new IllegalStateException("Menu item is not available: " + menuItem.getName());
        }

        // If item already in cart, increase quantity
        cartItemRepository.findByCart_IdAndMenuItem_Id(cart.getId(), menuItem.getId())
            .ifPresentOrElse(
                existingItem -> existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity()),
                () -> {
                    CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .menuItem(menuItem)
                        .quantity(request.getQuantity())
                        .build();
                    cart.getItems().add(newItem);
                }
            );

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItemQuantity(String username, Long menuItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(username);

        CartItem cartItem = cartItemRepository.findByCart_IdAndMenuItem_Id(cart.getId(), menuItemId)
            .orElseThrow(() -> new NoSuchElementException("Item not in cart"));

        cartItem.setQuantity(request.getQuantity());
        cartRepository.save(cart);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(String username, Long menuItemId) {
        Cart cart = getOrCreateCart(username);

        CartItem cartItem = cartItemRepository.findByCart_IdAndMenuItem_Id(cart.getId(), menuItemId)
            .orElseThrow(() -> new NoSuchElementException("Item not in cart"));

        cart.getItems().remove(cartItem);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // Scheduled job — runs every day at midnight
    // Clears carts not updated in 3 months
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void clearExpiredCarts() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(3);
        List<Cart> expiredCarts = cartRepository.findByUpdatedAtBefore(cutoff);
        expiredCarts.forEach(cart -> cart.getItems().clear());
        cartRepository.saveAll(expiredCarts);
        log.info("Cleared {} expired carts", expiredCarts.size());
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
            .map(this::toItemResponse)
            .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
            .map(CartItemResponse::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
            .cartId(cart.getId())
            .username(cart.getUser().getUsername())
            .items(itemResponses)
            .total(total)
            .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal subtotal = item.getMenuItem().getCost()
            .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
            .menuItemId(item.getMenuItem().getId())
            .itemName(item.getMenuItem().getName())
            .itemPrice(item.getMenuItem().getCost())
            .quantity(item.getQuantity())
            .subtotal(subtotal)
            .build();
    }
}
