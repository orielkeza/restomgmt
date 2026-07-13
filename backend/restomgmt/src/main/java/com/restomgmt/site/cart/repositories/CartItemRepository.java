package com.restomgmt.site.cart.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restomgmt.site.cart.models.CartItem;

public interface CartItemRepository extends JpaRepository <CartItem, Long> {
    Optional<CartItem> findByCart_IdAndMenuItem_Id(Long cartId, Long menuItemId);
}
