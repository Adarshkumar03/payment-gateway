package com.example.paymentGateway.service;

import com.example.paymentGateway.dto.PaymentRequestDTO;
import com.example.paymentGateway.dto.PaymentResponseDTO;
import com.example.paymentGateway.exception.*;
import com.example.paymentGateway.model.Account;
import com.example.paymentGateway.model.Payment;
import com.example.paymentGateway.model.PaymentStatus;
import com.example.paymentGateway.repository.AccountRepository;
import com.example.paymentGateway.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PaymentService {

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(AccountRepository accountRepository, PaymentRepository paymentRepository) {
        this.accountRepository = accountRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Debit, credit and payment record all happen in ONE transaction.
     * Any RuntimeException rolls everything back.
     *
     * @param username the authenticated caller; must own the sending account
     */
    @Transactional
    public PaymentResponseDTO transfer(PaymentRequestDTO request, String idempotencyKey, String username) {
        BigDecimal amount = request.amount().setScale(2, RoundingMode.UNNECESSARY);

        // 1. Basic validation
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new InvalidPaymentException("Sender and receiver accounts must be different");
        }

        // 2. Authorization: you can only send money OUT of your own account.
        //    This runs before the idempotency lookup so nobody can read someone else's payment by reusing a key.
        Account from = accountRepository.findById(request.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.fromAccountId()));
        if (!from.isOwnedBy(username)) {
            throw new ForbiddenException("You do not own account " + from.getId());
        }

        // 3. Idempotency: if we've seen this key, return the original result and move no money.
        var existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return replay(existing.get(), request, amount);
        }

        // 4. Remaining business validation
        Account to = accountRepository.findById(request.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.toAccountId()));
        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(from.getId());
        }

        // 5. Insert the payment FIRST and flush: the unique key constraint stops duplicate concurrent
        //    requests before any money has moved.
        Payment payment = paymentRepository.saveAndFlush(
                new Payment(from.getId(), to.getId(), amount, PaymentStatus.COMPLETED, idempotencyKey));

        // 6. Move the money. @Version on Account guards against concurrent modifications.
        from.debit(amount);
        to.credit(amount);
        accountRepository.save(from);
        accountRepository.save(to);

        return PaymentResponseDTO.from(payment);
    }

    /** A payment can be viewed by the owner of either the sending or receiving account. */
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPayment(Long id, String username) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        if (!owns(payment.getFromAccountId(), username) && !owns(payment.getToAccountId(), username)) {
            throw new ForbiddenException("You are not a party to payment " + id);
        }
        return PaymentResponseDTO.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByAccountId(Long accountId, String username) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        if (!account.isOwnedBy(username)) {
            throw new ForbiddenException("You do not own account " + accountId);
        }
        return paymentRepository.findAllByAccountId(accountId).stream()
                .map(PaymentResponseDTO::from)
                .toList();
    }

    private boolean owns(Long accountId, String username) {
        return accountRepository.findById(accountId)
                .map(a -> a.isOwnedBy(username))
                .orElse(false);
    }

    private PaymentResponseDTO replay(Payment original, PaymentRequestDTO request, BigDecimal amount) {
        boolean samePayload = original.getFromAccountId().equals(request.fromAccountId())
                && original.getToAccountId().equals(request.toAccountId())
                && original.getAmount().compareTo(amount) == 0;
        if (!samePayload) {
            throw new DuplicateRequestException("Idempotency-Key was already used with a different request");
        }
        return PaymentResponseDTO.from(original);
    }
}