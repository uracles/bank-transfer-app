package com.bank.transfer.app.service;

import com.bank.transfer.app.dto.TransactionFilterRequest;
import com.bank.transfer.app.dto.TransactionSummaryResponse;
import com.bank.transfer.app.dto.TransferRequest;
import com.bank.transfer.app.dto.TransferResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface TransferService {

    TransferResponse processTransfer(TransferRequest request);

    Page<TransferResponse> getTransactions(TransactionFilterRequest filter);

    TransactionSummaryResponse getSummaryForDate(LocalDate date);

    void processCommissions();

    void generateDailySummary(LocalDate date);
}
