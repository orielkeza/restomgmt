package com.restomgmt.site.order.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restomgmt.site.order.models.OrderItem;

public interface OrderItemRespository extends JpaRepository<OrderItem, Long> {
    
}
