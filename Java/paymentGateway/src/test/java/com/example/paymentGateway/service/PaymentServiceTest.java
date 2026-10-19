package com.example.paymentGateway.service;

import com.example.paymentGateway.dto.PaymentRequestDTO;
import com.example.paymentGateway.dto.PaymentResponseDTO;
import com.example.paymentGateway.exception.*;
import com.example.paymentGateway.model.Account;
import com.example.paymentGateway.model.Payment;
import com.example.paymentGateway.model.PaymentStatus;
import com.example.paymentGateway.repository.AccountRepository;
import com.example.paymentGateway.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock PaymentRepository paymentRepository;
    @InjectMocks PaymentService paymentService;

    Account alice;
    Account bob;

    @BeforeEach
    void setUp() {
        alice = new Account("Alice", new BigDecimal("100.00"), "alice");
        alice.setId(1L);
        bob = new Account("Bob", new BigDecimal("50.00"), "bob");
        bob.setId(2L);
    }

    @Test
    void successfulTransferUpdatesBothBalances() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponseDTO result = paymentService.transfer(
                new PaymentRequestDTO(1L, 2L, new BigDecimal("30.00")), "key-1", "alice");

        assertEquals(PaymentStatus.COMPLETED, result.status());
        assertEquals(new BigDecimal("70.00"), alice.getBalance());
        assertEquals(new BigDecimal("80.00"), bob.getBalance());
    }

    @Test
    void insufficientFundsChangesNothing() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(paymentRepository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());

        assertThrows(InsufficientFundsException.class, () -> paymentService.transfer(
                new PaymentRequestDTO(1L, 2L, new BigDecimal("500.00")), "key-2", "alice"));

        assertEquals(new BigDecimal("100.00"), alice.getBalance());
        assertEquals(new BigDecimal("50.00"), bob.getBalance());
        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void duplicateIdempotencyKeyReturnsOriginalAndDoesNotTransferAgain() {
        Payment original = new Payment(1L, 2L, new BigDecimal("30.00"), PaymentStatus.COMPLETED, "key-3");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(paymentRepository.findByIdempotencyKey("key-3")).thenReturn(Optional.of(original));

        PaymentResponseDTO result = paymentService.transfer(
                new PaymentRequestDTO(1L, 2L, new BigDecimal("30.00")), "key-3", "alice");

        assertEquals("key-3", result.idempotencyKey());
        assertEquals(new BigDecimal("100.00"), alice.getBalance());
        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void sameKeyWithDifferentPayloadIsRejected() {
        Payment original = new Payment(1L, 2L, new BigDecimal("30.00"), PaymentStatus.COMPLETED, "key-4");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(paymentRepository.findByIdempotencyKey("key-4")).thenReturn(Optional.of(original));

        assertThrows(DuplicateRequestException.class, () -> paymentService.transfer(
                new PaymentRequestDTO(1L, 2L, new BigDecimal("99.00")), "key-4", "alice"));
    }

    @Test
    void unknownReceiverThrowsNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        when(paymentRepository.findByIdempotencyKey("key-5")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> paymentService.transfer(
                new PaymentRequestDTO(1L, 99L, new BigDecimal("10.00")), "key-5", "alice"));
        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void sameSenderAndReceiverIsRejected() {
        assertThrows(InvalidPaymentException.class, () -> paymentService.transfer(
                new PaymentRequestDTO(1L, 1L, new BigDecimal("10.00")), "key-6", "alice"));
    }

    @Test
    void cannotSendFromSomeoneElsesAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(alice));

        assertThrows(ForbiddenException.class, () -> paymentService.transfer(
                new PaymentRequestDTO(1L, 2L, new BigDecimal("10.00")), "key-7", "mallory"));

        assertEquals(new BigDecimal("100.00"), alice.getBalance());
        verify(paymentRepository, never()).saveAndFlush(any());
    }
}