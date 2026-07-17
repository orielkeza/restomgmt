package com.restomgmt.site.order.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restomgmt.site.cart.models.Cart;
import com.restomgmt.site.cart.models.CartItem;
import com.restomgmt.site.cart.repositories.CartRepository;
import com.restomgmt.site.order.dto.AssignRiderRequest;
import com.restomgmt.site.order.dto.OrderItemResponse;
import com.restomgmt.site.order.dto.OrderResponse;
import com.restomgmt.site.order.dto.UpdateOrderStatusRequest;
import com.restomgmt.site.order.models.Order;
import com.restomgmt.site.order.models.OrderDetails;
import com.restomgmt.site.order.models.OrderItem;
import com.restomgmt.site.order.models.OrderStatus;
import com.restomgmt.site.order.repositories.OrderDetailsRepository;
import com.restomgmt.site.order.repositories.OrderRepository;
import com.restomgmt.site.payment.repositories.PaymentRepository;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;

    private final OrderDetailsRepository orderDetailsRepository;

    private final CartRepository cartRepository;

    private final UserRepository userRepository;

    private final PaymentRepository paymentRepository;

    @Transactional
    public OrderResponse placeOrder(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        Cart cart = cartRepository.findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order with empty cart");
        }

        List<String> warnings = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
            .user(user)
            .status(OrderStatus.PENDING)
            .items(orderItems)
            .build();

        // Snapshot items from cart
        for (CartItem cartItem : cart.getItems()) {
            if (!cartItem.getMenuItem().isAvailable()) {
                warnings.add(cartItem.getMenuItem().getName() +
                    " is no longer available and was skipped");
                continue;
            }

            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .menuItem(cartItem.getMenuItem())
                .quantity(cartItem.getQuantity())
                .priceAtOrder(cartItem.getMenuItem().getCost())
                .build();

            orderItems.add(orderItem);
        }

        if (orderItems.isEmpty()) {
            throw new IllegalStateException(
                "No available items in cart to order");
        }

        Order savedOrder = orderRepository.save(order);

        // Create order details
        OrderDetails details = OrderDetails.builder()
            .order(savedOrder)
            .status(OrderStatus.PENDING)
            .build();
        orderDetailsRepository.save(details);

        // Clear the cart
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order placed for user={} orderId={}", username, savedOrder.getId());

        return toResponse(savedOrder, warnings);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String username) {
        return orderRepository.findByUsername(username)
            .stream()
            .map(o -> toResponse(o, List.of()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String username, Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new NoSuchElementException("Order not found"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have access to this order");
        }

        return toResponse(order, List.of());
    }

    @Transactional
    public OrderResponse cancelOrder(String username, Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new NoSuchElementException("Order not found"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have access to this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }

        LocalDateTime cutoff = order.getCreatedAt().plusMinutes(30);
        if (LocalDateTime.now().isAfter(cutoff)) {
            throw new IllegalStateException(
                "Cancellation window of 30 minutes has passed");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderDetailsRepository.findByOrder_Id(orderId).ifPresent(
            details -> details.setStatus(OrderStatus.CANCELLED)
        );

        // Flag refund if payment was successful
        paymentRepository.findByOrder_Id(orderId).ifPresent(payment -> {
            if (payment.getStatus() == com.restomgmt.site.payment.models.PaymentStatus.SUCCESSFUL) {
                payment.setRefundFlagged(true);
                paymentRepository.save(payment);
                log.info("Refund flagged for cancelled order {}", orderId);
            }
        });

        return toResponse(orderRepository.save(order), List.of());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
            .stream()
            .map(o -> toResponse(o, List.of()))
            .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse advanceOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new NoSuchElementException("Order not found"));

        if (!order.getStatus().canAdvance()) {
            throw new IllegalStateException(
                "Order cannot be advanced from status: " + order.getStatus());
        }

        OrderStatus nextStatus = order.getStatus().next();

        if (request.getStatus() != nextStatus) {
            throw new IllegalStateException(
                "Invalid status transition. Expected: " + nextStatus +
                " but got: " + request.getStatus());
        }

        order.setStatus(nextStatus);
        orderDetailsRepository.findByOrder_Id(orderId).ifPresent(
            details -> details.setStatus(nextStatus)
        );

        log.info("Order {} advanced to {}", orderId, nextStatus);

        return toResponse(orderRepository.save(order), List.of());
    }

    private OrderResponse toResponse(Order order, List<String> warnings) {
        List<OrderItemResponse> itemResponses = order.getItems()
            .stream()
            .map(this::toItemResponse)
            .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
            .map(OrderItemResponse::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderResponse.builder()
            .orderId(order.getId())
            .username(order.getUser().getUsername())
            .status(order.getStatus())
            .items(itemResponses)
            .total(total)
            .warnings(warnings)
            .riderPhone(order.getRiderPhone())
            .deliveryNote(order.getDeliveryNote())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getPriceAtOrder()
            .multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemResponse.builder()
            .menuItemId(item.getMenuItem().getId())
            .itemName(item.getMenuItem().getName())
            .priceAtOrder(item.getPriceAtOrder())
            .quantity(item.getQuantity())
            .subtotal(subtotal)
            .build();
    }

    @Transactional
    public OrderResponse assignRider(Long orderId, AssignRiderRequest request) {
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new NoSuchElementException("Order not found"));

        if (order.getStatus() != OrderStatus.OUTFORDELIVERY) {
            throw new IllegalStateException(
                "Rider can only be assigned when order is OUT_FOR_DELIVERY");
        }

        order.setRiderPhone(request.getRiderPhone());
        order.setDeliveryNote(request.getDeliveryNote());

        log.info("Rider assigned to order {} - phone: {}", orderId, request.getRiderPhone());

        return toResponse(orderRepository.save(order), List.of());
    }

}
