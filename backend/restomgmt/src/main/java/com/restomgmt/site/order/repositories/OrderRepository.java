package com.restomgmt.site.order.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restomgmt.site.order.models.Order;
import com.restomgmt.site.order.models.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.user.username = :username")
    List<Order> findByUsername(String username);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.menuItem WHERE o.id = :id")
    Optional<Order> findByIdWithItems(Long id);

    List<Order> findByStatus(OrderStatus status);
    
}