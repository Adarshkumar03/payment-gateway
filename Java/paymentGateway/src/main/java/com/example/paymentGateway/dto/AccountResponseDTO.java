package com.example.paymentGateway.dto;

import com.example.paymentGateway.model.Account;

import java.math.BigDecimal;

public record AccountResponseDTO(Long id, String holderName, BigDecimal balance) {

    public static AccountResponseDTO from(Account account) {
        return new AccountResponseDTO(account.getId(), account.getHolderName(), account.getBalance());
    }
}