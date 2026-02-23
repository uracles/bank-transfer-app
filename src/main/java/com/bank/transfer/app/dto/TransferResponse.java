package com.bank.transfer.app.dto;

import com.bank.transfer.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private String transactionReference;
    private BigDecimal amount;
    private BigDecimal transactionFee;
    private BigDecimal billedAmount;
    private String description;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private TransactionStatus status;
    private String statusMessage;
    private Boolean commissionWorthy;
    private BigDecimal commission;
    private LocalDateTime createdAt;
}
