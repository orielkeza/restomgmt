package com.restomgmt.site.payment.repositories;

import com.restomgmt.site.payment.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_Id(Long orderId);
    Optional<Payment> findByMomoReferenceId(String momoReferenceId);
}