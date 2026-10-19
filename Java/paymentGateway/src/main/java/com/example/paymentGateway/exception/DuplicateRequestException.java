package com.example.paymentGateway.exception;

/** Thrown when an Idempotency-Key is reused with a different payload. */
public class DuplicateRequestException extends RuntimeException {
    public DuplicateRequestException(String message) {
        super(message);
    }
}