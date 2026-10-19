package com.example.paymentGateway.controller;

import com.example.paymentGateway.dto.AccountCreateRequestDTO;
import com.example.paymentGateway.dto.AccountListDTO;
import com.example.paymentGateway.dto.AccountResponseDTO;
import com.example.paymentGateway.dto.PaymentResponseDTO;
import com.example.paymentGateway.service.AccountService;
import com.example.paymentGateway.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final PaymentService paymentService;

    public AccountController(AccountService accountService, PaymentService paymentService) {
        this.accountService = accountService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(
            @Valid @RequestBody AccountCreateRequestDTO request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<AccountListDTO>> getAccounts(Principal principal) {
        return ResponseEntity.ok(accountService.getAllAccounts(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(accountService.getAccountById(id, principal.getName()));
    }

    @GetMapping("/{accountId}/payments")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByAccountId(
            @PathVariable Long accountId, Principal principal) {
        return ResponseEntity.ok(paymentService.getPaymentsByAccountId(accountId, principal.getName()));
    }
}