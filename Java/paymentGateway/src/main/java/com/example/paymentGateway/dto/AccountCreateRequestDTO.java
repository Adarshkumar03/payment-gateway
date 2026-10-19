package com.example.paymentGateway.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountCreateRequestDTO(
        @NotBlank(message = "Account holder name is required")
        String holderName,

        @NotNull(message = "Balance is required")
        @PositiveOrZero(message = "Balance cannot be negative")
        @Digits(integer = 15, fraction = 2, message = "Balance must have at most 2 decimal places")
        BigDecimal balance
) {
}