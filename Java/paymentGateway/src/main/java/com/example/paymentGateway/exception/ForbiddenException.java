package com.example.paymentGateway.exception;

/** The caller is logged in, but is not allowed to touch this resource. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}