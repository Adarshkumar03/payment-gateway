package com.example.paymentGateway.repository;

import com.example.paymentGateway.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT p FROM Payment p WHERE p.fromAccountId = :accountId OR p.toAccountId = :accountId ORDER BY p.createdAt DESC")
    List<Payment> findAllByAccountId(@Param("accountId") Long accountId);
}