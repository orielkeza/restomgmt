package com.restomgmt.site.cart.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restomgmt.site.cart.dto.AddToCartRequest;
import com.restomgmt.site.cart.dto.CartResponse;
import com.restomgmt.site.cart.dto.UpdateCartItemRequest;
import com.restomgmt.site.cart.models.Cart;
import com.restomgmt.site.cart.models.CartItem;
import com.restomgmt.site.cart.repositories.CartItemRepository;
import com.restomgmt.site.cart.repositories.CartRepository;
import com.restomgmt.site.menu.models.Category;
import com.restomgmt.site.menu.models.MenuItem;
import com.restomgmt.site.menu.repositories.MenuItemRepository;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().name("Mains").build();

        user = User.builder()
            .username("testuser")
            .email("test@test.com")
            .password("encoded")
            .enabled(true)
            .tokenExpired(false)
            .build();

        cart = Cart.builder()
            .user(user)
            .items(new ArrayList<>())
            .build();

        menuItem = MenuItem.builder()
            .name("Grilled Chicken")
            .description("Herb-marinated grilled chicken")
            .cost(new BigDecimal("12.99"))
            .available(true)
            .category(category)
            .build();
    }

    // getCart
    @Test
    void getCartShouldReturnExistingCart() {
        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));

        CartResponse result = cartService.getCart("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertTrue(result.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotal());
    }

    @Test
    void getCartShouldCreateNewCartWhenNoneExists() {
        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse result = cartService.getCart("testuser");

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCartShouldThrowWhenUserNotFound() {
        when(cartRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> cartService.getCart("ghost"));
    }

    // addItem
    @Test
    void addItemShouldAddNewItemToCart() {
        AddToCartRequest request = new AddToCartRequest();
        request.setMenuItemId(1L);
        request.setQuantity(2);

        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(cartItemRepository.findByCart_IdAndMenuItem_Id(any(), any()))
            .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse result = cartService.addItem("testuser", request);

        assertNotNull(result);
        verify(cartRepository).save(cart);
    }

    @Test
    void addItemShouldIncreaseQuantityWhenItemAlreadyInCart() {
        CartItem existingItem = CartItem.builder()
            .cart(cart)
            .menuItem(menuItem)
            .quantity(1)
            .build();

        AddToCartRequest request = new AddToCartRequest();
        request.setMenuItemId(1L);
        request.setQuantity(2);

        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(cartItemRepository.findByCart_IdAndMenuItem_Id(any(), any()))
            .thenReturn(Optional.of(existingItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addItem("testuser", request);

        assertEquals(3, existingItem.getQuantity());
    }

    @Test
    void addItemShouldThrowWhenMenuItemNotFound() {
        AddToCartRequest request = new AddToCartRequest();
        request.setMenuItemId(99L);
        request.setQuantity(1);

        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> cartService.addItem("testuser", request));
    }

    @Test
    void addItemShouldThrowWhenItemIsUnavailable() {
        menuItem.setAvailable(false);

        AddToCartRequest request = new AddToCartRequest();
        request.setMenuItemId(1L);
        request.setQuantity(1);

        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        assertThrows(IllegalStateException.class,
            () -> cartService.addItem("testuser", request));
        verify(cartRepository, never()).save(any());
    }

    // updateItemQuantity
    @Test
    void updateItemQuantityShouldUpdateQuantity() {
        CartItem cartItem = CartItem.builder()
            .cart(cart)
            .menuItem(menuItem)
            .quantity(1)
            .build();
        cart.getItems().add(cartItem);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_IdAndMenuItem_Id(any(), any()))
            .thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.updateItemQuantity("testuser", 1L, request);

        assertEquals(5, cartItem.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void updateItemQuantityShouldThrowWhenItemNotInCart() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_IdAndMenuItem_Id(any(), any()))
            .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> cartService.updateItemQuantity("testuser", 99L, request));
        verify(cartRepository, never()).save(any());
    }

    // removeItem
    @Test
    void removeItemShouldRemoveItemFromCart() {
        CartItem cartItem = CartItem.builder()
            .cart(cart)
            .menuItem(menuItem)
            .quantity(2)
            .build();
        cart.getItems().add(cartItem);

        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_IdAndMenuItem_Id(any(), any()))
            .thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.removeItem("testuser", 1L);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItemShouldThrowWhenItemNotInCart() {
        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_IdAndMenuItem_Id(any(), any()))
            .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> cartService.removeItem("testuser", 99L));
    }

    // clearCart
    @Test
    void clearCartShouldRemoveAllItems() {
        CartItem cartItem = CartItem.builder()
            .cart(cart)
            .menuItem(menuItem)
            .quantity(1)
            .build();
        cart.getItems().add(cartItem);

        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clearCart("testuser");

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    // total calculation
    @Test
    void getCartShouldCalculateTotalCorrectly() {
        MenuItem secondItem = MenuItem.builder()
            .name("Lemonade")
            .cost(new BigDecimal("2.99"))
            .available(true)
            .category(Category.builder().name("Drinks").build())
            .build();

        CartItem item1 = CartItem.builder()
            .cart(cart).menuItem(menuItem).quantity(2).build();
        CartItem item2 = CartItem.builder()
            .cart(cart).menuItem(secondItem).quantity(1).build();

        cart.getItems().add(item1);
        cart.getItems().add(item2);

        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));

        CartResponse result = cartService.getCart("testuser");

        // 2 * 12.99 + 1 * 2.99 = 28.97
        assertEquals(new BigDecimal("28.97"), result.getTotal());
    }
}