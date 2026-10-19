package com.example.paymentGateway.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long accountId) {
        super("Insufficient funds in account " + accountId);
    }
}