package com.example.paymentGateway.dto;

import com.example.paymentGateway.model.Payment;
import com.example.paymentGateway.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponseDTO(
        Long id,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        PaymentStatus status,
        String idempotencyKey,
        Instant createdAt
) {
    public static PaymentResponseDTO from(Payment p) {
        return new PaymentResponseDTO(p.getId(), p.getFromAccountId(), p.getToAccountId(),
                p.getAmount(), p.getStatus(), p.getIdempotencyKey(), p.getCreatedAt());
    }
}