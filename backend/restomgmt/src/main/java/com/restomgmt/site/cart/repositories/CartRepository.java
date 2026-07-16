package com.restomgmt.site.cart.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restomgmt.site.cart.models.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.user.username = :username")
    Optional<Cart> findByUsername(String username);

    List<Cart> findByUpdatedAtBefore(LocalDateTime cutoff);
}
