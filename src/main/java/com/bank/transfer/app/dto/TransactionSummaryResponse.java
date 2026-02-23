package com.bank.transfer.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryResponse {

    private LocalDate summaryDate;
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private Long insufficientFundTransactions;
    private BigDecimal totalAmount;
    private BigDecimal totalFees;
    private BigDecimal totalCommissions;
    private Long commissionWorthyCount;
}
