package com.example.paymentGateway.controller;

import com.example.paymentGateway.dto.PaymentRequestDTO;
import com.example.paymentGateway.dto.PaymentResponseDTO;
import com.example.paymentGateway.exception.InvalidPaymentException;
import com.example.paymentGateway.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequestDTO request,
            Principal principal) {
        if (idempotencyKey.isBlank()) {
            throw new InvalidPaymentException("Idempotency-Key must not be blank");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.transfer(request, idempotencyKey, principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(paymentService.getPayment(id, principal.getName()));
    }
}