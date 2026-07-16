package com.restomgmt.site.order.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.restomgmt.site.cart.models.Cart;
import com.restomgmt.site.cart.models.CartItem;
import com.restomgmt.site.cart.repositories.CartRepository;
import com.restomgmt.site.menu.models.Category;
import com.restomgmt.site.menu.models.MenuItem;
import com.restomgmt.site.order.dto.AssignRiderRequest;
import com.restomgmt.site.order.dto.OrderResponse;
import com.restomgmt.site.order.dto.UpdateOrderStatusRequest;
import com.restomgmt.site.order.models.Order;
import com.restomgmt.site.order.models.OrderDetails;
import com.restomgmt.site.order.models.OrderItem;
import com.restomgmt.site.order.models.OrderStatus;
import com.restomgmt.site.order.repositories.OrderDetailsRepository;
import com.restomgmt.site.order.repositories.OrderRepository;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderDetailsRepository orderDetailsRepository;
    
    @Mock
    private CartRepository cartRepository;
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private MenuItem menuItem;
    private CartItem cartItem;
    private Order order;

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

        menuItem = MenuItem.builder()
            .name("Grilled Chicken")
            .description("Herb-marinated grilled chicken")
            .cost(new BigDecimal("12.99"))
            .available(true)
            .category(category)
            .build();
        ReflectionTestUtils.setField(menuItem, "id", 1L);

        cartItem = CartItem.builder()
            .menuItem(menuItem)
            .quantity(2)
            .build();

        cart = Cart.builder()
            .user(user)
            .items(new ArrayList<>(List.of(cartItem)))
            .build();

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
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
    }

    // placeOrder
    @Test
    void placeOrderShouldCreateOrderFromCart() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailsRepository.save(any(OrderDetails.class)))
            .thenReturn(OrderDetails.builder().order(order).status(OrderStatus.PENDING).build());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        OrderResponse result = orderService.placeOrder("testuser");

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals("testuser", result.getUsername());
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void placeOrderShouldThrowWhenCartIsEmpty() {
        cart.getItems().clear();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));

        assertThrows(IllegalStateException.class,
            () -> orderService.placeOrder("testuser"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrderShouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> orderService.placeOrder("ghost"));
    }

    @Test
    void placeOrderShouldThrowWhenCartNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> orderService.placeOrder("testuser"));
    }

    @Test
    void placeOrderShouldSkipUnavailableItemsAndWarn() {
        menuItem.setAvailable(false);

        MenuItem availableItem = MenuItem.builder()
            .name("Lemonade")
            .cost(new BigDecimal("2.99"))
            .available(true)
            .category(Category.builder().name("Drinks").build())
            .build();

        CartItem availableCartItem = CartItem.builder()
            .menuItem(availableItem)
            .quantity(1)
            .build();

        cart.getItems().add(availableCartItem);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailsRepository.save(any(OrderDetails.class)))
            .thenReturn(OrderDetails.builder().order(order).status(OrderStatus.PENDING).build());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        OrderResponse result = orderService.placeOrder("testuser");

        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().get(0).contains("Grilled Chicken"));
    }

    @Test
    void placeOrderShouldThrowWhenAllItemsUnavailable() {
        menuItem.setAvailable(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUsername("testuser")).thenReturn(Optional.of(cart));

        assertThrows(IllegalStateException.class,
            () -> orderService.placeOrder("testuser"));
    }

    // getUserOrders
    @Test
    void getUserOrdersShouldReturnOrderList() {
        when(orderRepository.findByUsername("testuser")).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getUserOrders("testuser");

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void getUserOrdersShouldReturnEmptyListWhenNoOrders() {
        when(orderRepository.findByUsername("testuser")).thenReturn(List.of());

        List<OrderResponse> result = orderService.getUserOrders("testuser");

        assertTrue(result.isEmpty());
    }

    // getOrderById
    @Test
    void getOrderByIdShouldReturnOrderWhenOwner() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderById("testuser", 1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getOrderByIdShouldThrowWhenNotOwner() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class,
            () -> orderService.getOrderById("otheruser", 1L));
    }

    @Test
    void getOrderByIdShouldThrowWhenNotFound() {
        when(orderRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> orderService.getOrderById("testuser", 99L));
    }

    // cancelOrder
    @Test
    void cancelOrderShouldCancelWhenWithin30Minutes() {
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusMinutes(10));

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderDetailsRepository.findByOrder_Id(1L)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse result = orderService.cancelOrder("testuser", 1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelOrderShouldThrowWhenOutside30Minutes() {
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusMinutes(31));

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
            () -> orderService.cancelOrder("testuser", 1L));
    }

    @Test
    void cancelOrderShouldThrowWhenNotPending() {
        order.setStatus(OrderStatus.CONFIRMED);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusMinutes(5));

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
            () -> orderService.cancelOrder("testuser", 1L));
    }

    @Test
    void cancelOrderShouldThrowWhenNotOwner() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class,
            () -> orderService.cancelOrder("otheruser", 1L));
    }

    // advanceOrderStatus
    @Test
    void advanceOrderStatusShouldAdvanceToNextStatus() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderDetailsRepository.findByOrder_Id(1L)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse result = orderService.advanceOrderStatus(1L, request);

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void advanceOrderStatusShouldThrowWhenInvalidTransition() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
            () -> orderService.advanceOrderStatus(1L, request));
    }

    @Test
    void advanceOrderStatusShouldThrowWhenOrderDelivered() {
        order.setStatus(OrderStatus.DELIVERED);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
            () -> orderService.advanceOrderStatus(1L, request));
    }

    @Test
    void advanceOrderStatusShouldThrowWhenOrderCancelled() {
        order.setStatus(OrderStatus.CANCELLED);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
            () -> orderService.advanceOrderStatus(1L, request));
    }

    // getAllOrders
    @Test
    void getAllOrdersShouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getAllOrders();

        assertEquals(1, result.size());
    }

    @Test
    void getAllOrdersShouldReturnEmptyListWhenNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<OrderResponse> result = orderService.getAllOrders();

        assertTrue(result.isEmpty());
    }

    // assignRider
    @Test
    void assignRiderShouldSetRiderPhoneWhenOrderIsOutForDelivery() {
        order.setStatus(OrderStatus.OUTFORDELIVERY);

        AssignRiderRequest request = new AssignRiderRequest();
        request.setRiderPhone("+250788123456");
        request.setDeliveryNote("Call on arrival");

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse result = orderService.assignRider(1L, request);

        assertNotNull(result);
        verify(orderRepository).save(argThat(o ->
            "+250788123456".equals(o.getRiderPhone()) &&
            "Call on arrival".equals(o.getDeliveryNote())
        ));
    }

    @Test
    void assignRiderShouldThrowWhenOrderIsNotOutForDelivery() {
        order.setStatus(OrderStatus.READY);

        AssignRiderRequest request = new AssignRiderRequest();
        request.setRiderPhone("+250788123456");

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
            () -> orderService.assignRider(1L, request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void assignRiderShouldThrowWhenOrderNotFound() {
        AssignRiderRequest request = new AssignRiderRequest();
        request.setRiderPhone("+250788123456");

        when(orderRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> orderService.assignRider(99L, request));
    }
}
