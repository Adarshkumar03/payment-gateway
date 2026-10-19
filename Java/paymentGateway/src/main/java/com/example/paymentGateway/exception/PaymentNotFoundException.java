package com.example.paymentGateway.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) {
        super("Payment not found: " + id);
    }
}