package com.bank.transfer.app.config;

import com.bank.transfer.app.dto.TransferResponse;
import com.bank.transfer.app.dto.TransactionSummaryResponse;
import com.bank.transfer.app.entity.Transaction;
import com.bank.transfer.app.entity.TransactionSummary;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransferResponse toTransferResponse(Transaction tx) {
        return TransferResponse.builder()
                .transactionReference(tx.getTransactionReference())
                .amount(tx.getAmount())
                .transactionFee(tx.getTransactionFee())
                .billedAmount(tx.getBilledAmount())
                .description(tx.getDescription())
                .sourceAccountNumber(tx.getSourceAccountNumber())
                .destinationAccountNumber(tx.getDestinationAccountNumber())
                .status(tx.getStatus())
                .statusMessage(tx.getStatusMessage())
                .commissionWorthy(tx.getCommissionWorthy())
                .commission(tx.getCommission())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    public TransactionSummaryResponse toSummaryResponse(TransactionSummary s) {
        return TransactionSummaryResponse.builder()
                .summaryDate(s.getSummaryDate())
                .totalTransactions(s.getTotalTransactions())
                .successfulTransactions(s.getSuccessfulTransactions())
                .failedTransactions(s.getFailedTransactions())
                .insufficientFundTransactions(s.getInsufficientFundTransactions())
                .totalAmount(s.getTotalAmount())
                .totalFees(s.getTotalFees())
                .totalCommissions(s.getTotalCommissions())
                .commissionWorthyCount(s.getCommissionWorthyCount())
                .build();
    }
}