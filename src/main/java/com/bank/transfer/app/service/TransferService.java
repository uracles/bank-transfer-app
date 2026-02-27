package com.bank.transfer.app.service;

import com.bank.transfer.app.dto.TransactionFilterRequest;
import com.bank.transfer.app.dto.TransactionSummaryResponse;
import com.bank.transfer.app.dto.TransferRequest;
import com.bank.transfer.app.dto.TransferResponse;
import com.bank.transfer.app.util.PagedResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface TransferService {

    TransferResponse processTransfer(TransferRequest request);

    PagedResponse<List<TransferResponse>> getTransactions(TransactionFilterRequest filter, Integer page, Integer pageSize);

    TransactionSummaryResponse getSummaryForDate(LocalDate date);

    void processCommissions();

    void generateDailySummary(LocalDate date);

    TransactionSummaryResponse generateDailySummaryAndFetch(LocalDate date);
}
