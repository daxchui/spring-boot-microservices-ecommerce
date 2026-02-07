package com.comp5348.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a transfer or refund transaction between accounts.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transfer", indexes = {
        @Index(name = "idx_idempotency_key", columnList = "idempotencyKey"),
        @Index(name = "idx_correlation_id", columnList = "correlationId")
})
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String correlationId;

    @Column(unique = true)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private AccountEntity fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private AccountEntity toAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferType type = TransferType.CHARGE;

    @Column
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime completedAt;

    @Column
    private Long orderId;

    public enum TransferStatus {
        PENDING, SUCCEEDED, FAILED
    }

    public enum TransferType {
        CHARGE, REFUND
    }
}
