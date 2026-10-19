package com.example.paymentGateway.repository;

import com.example.paymentGateway.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOwnerUsername(String ownerUsername);
}