package com.example.paymentGateway.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequestDTO(
        @NotNull(message = "fromAccountId is required")
        Long fromAccountId,

        @NotNull(message = "toAccountId is required")
        Long toAccountId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be positive")
        @Digits(integer = 15, fraction = 2, message = "amount must have at most 2 decimal places")
        BigDecimal amount
) {
}