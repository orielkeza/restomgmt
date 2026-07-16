package com.restomgmt.site.order.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restomgmt.site.order.models.OrderDetails;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {
 
    Optional<OrderDetails> findByOrder_Id(Long orderId);
    
}