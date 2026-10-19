package com.example.paymentGateway.service;

import com.example.paymentGateway.dto.AccountCreateRequestDTO;
import com.example.paymentGateway.dto.AccountListDTO;
import com.example.paymentGateway.dto.AccountResponseDTO;
import com.example.paymentGateway.exception.AccountNotFoundException;
import com.example.paymentGateway.exception.ForbiddenException;
import com.example.paymentGateway.model.Account;
import com.example.paymentGateway.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /** Only the caller's own accounts. */
    @Transactional(readOnly = true)
    public List<AccountListDTO> getAllAccounts(String username) {
        return accountRepository.findByOwnerUsername(username).stream()
                .map(acc -> new AccountListDTO(acc.getId(), acc.getHolderName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountById(Long id, String username) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        if (!account.isOwnedBy(username)) {
            throw new ForbiddenException("You do not own account " + id);
        }
        return AccountResponseDTO.from(account);
    }

    @Transactional
    public AccountResponseDTO createAccount(AccountCreateRequestDTO request, String username) {
        Account account = new Account(
                request.holderName().trim(),
                request.balance().setScale(2, RoundingMode.UNNECESSARY),
                username);
        return AccountResponseDTO.from(accountRepository.save(account));
    }
}