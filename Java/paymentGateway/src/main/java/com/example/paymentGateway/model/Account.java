package com.example.paymentGateway.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    // Username of the AppUser who owns this account
    @Column(nullable = false)
    private String ownerUsername;

    // Optimistic locking: if two transactions update the same row, the loser fails
    @Version
    private Integer version;

    protected Account() {
        // required by JPA
    }

    public Account(String holderName, BigDecimal balance, String ownerUsername) {
        this.holderName = holderName;
        this.balance = balance;
        this.ownerUsername = ownerUsername;
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public boolean isOwnedBy(String username) {
        return ownerUsername != null && ownerUsername.equals(username);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getOwnerUsername() { return ownerUsername; }

    public Integer getVersion() { return version; }
}