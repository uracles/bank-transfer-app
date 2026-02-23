package com.bank.transfer.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_summaries",
        uniqueConstraints = @UniqueConstraint(columnNames = "summaryDate"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate summaryDate;

    @Column(nullable = false)
    private Long totalTransactions;

    @Column(nullable = false)
    private Long successfulTransactions;

    @Column(nullable = false)
    private Long failedTransactions;

    @Column(nullable = false)
    private Long insufficientFundTransactions;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalFees;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalCommissions;

    @Column(nullable = false)
    private Long commissionWorthyCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
