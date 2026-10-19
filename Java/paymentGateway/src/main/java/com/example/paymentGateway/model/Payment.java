package com.example.paymentGateway.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = @UniqueConstraint(name = "uk_payment_idempotency_key", columnNames = "idempotencyKey")
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fromAccountId;

    @Column(nullable = false)
    private Long toAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // The unique constraint is what makes idempotency safe under concurrent requests
    @Column(nullable = false, updatable = false)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Payment() {
        // required by JPA
    }

    public Payment(Long fromAccountId, Long toAccountId, BigDecimal amount,
                   PaymentStatus status, String idempotencyKey) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getId() { return id; }
    public Long getFromAccountId() { return fromAccountId; }
    public Long getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
}