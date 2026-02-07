
package com.comp5348.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a ledger entry in double-entry bookkeeping.
 * Each transfer creates two entries: one debit, one credit.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ledger_entry", indexes = {
        @Index(name = "idx_transfer_id", columnList = "transferId"),
        @Index(name = "idx_account_id", columnList = "accountId")
})
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transferId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal delta;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
