package com.bank.transfer.app.entity;

import com.bank.transfer.app.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_account_number", columnList = "sourceAccountNumber"),
        @Index(name = "idx_dest_account_number", columnList = "destinationAccountNumber"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionReference;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(precision = 19, scale = 4)
    private BigDecimal transactionFee;

    @Column(precision = 19, scale = 4)
    private BigDecimal billedAmount;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 20)
    private String sourceAccountNumber;

    @Column(nullable = false, length = 20)
    private String destinationAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(length = 500)
    private String statusMessage;

    @Column(nullable = false)
    @Builder.Default
    private Boolean commissionWorthy = false;

    @Column(precision = 19, scale = 4)
    private BigDecimal commission;

    @Column
    private Boolean commissionProcessed;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
