package com.bank.transfer.app.service.impl;

import com.bank.transfer.app.config.TransactionMapper;
import com.bank.transfer.app.dto.TransactionFilterRequest;
import com.bank.transfer.app.dto.TransactionSummaryResponse;
import com.bank.transfer.app.dto.TransferRequest;
import com.bank.transfer.app.dto.TransferResponse;
import com.bank.transfer.app.entity.Account;
import com.bank.transfer.app.entity.Transaction;
import com.bank.transfer.app.entity.TransactionSummary;
import com.bank.transfer.app.enums.TransactionStatus;
import com.bank.transfer.app.exception.AccountNotFoundException;
import com.bank.transfer.app.exception.InsufficientFundsException;
import com.bank.transfer.app.repository.AccountRepository;
import com.bank.transfer.app.repository.TransactionRepository;
import com.bank.transfer.app.repository.TransactionSpecification;
import com.bank.transfer.app.repository.TransactionSummaryRepository;
import com.bank.transfer.app.service.FeeCalculationService;
import com.bank.transfer.app.service.TransferService;
import com.bank.transfer.app.util.AppUtil;
import com.bank.transfer.app.util.PagedResponse;
import com.bank.transfer.app.util.ReferenceGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionSummaryRepository summaryRepository;
    private final FeeCalculationService feeCalculationService;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransferResponse processTransfer(TransferRequest request) {
        validateTransferRequest(request);

        String reference = ReferenceGeneratorUtil.generate();
        BigDecimal fee = feeCalculationService.calculateTransactionFee(request.getAmount());
        BigDecimal billedAmount = request.getAmount().add(fee);

        Transaction transaction = Transaction.builder()
                .transactionReference(reference)
                .amount(request.getAmount())
                .transactionFee(fee)
                .billedAmount(billedAmount)
                .description(request.getDescription())
                .sourceAccountNumber(request.getSourceAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .status(TransactionStatus.PENDING)
                .commissionWorthy(false)
                .build();

        try {
            executeTransfer(request, transaction, billedAmount);
        } catch (InsufficientFundsException e) {
            transaction.setStatus(TransactionStatus.INSUFFICIENT_FUND);
            transaction.setStatusMessage(e.getMessage());
            transactionRepository.save(transaction);
            throw e;
        } catch (Exception e) {
            log.error("Transfer failed for reference {}: {}", reference, e.getMessage(), e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setStatusMessage("Transfer failed due to internal error");
            transactionRepository.save(transaction);
            throw e;
        }

        return transactionMapper.toTransferResponse(transaction);
    }

    private void executeTransfer(TransferRequest request, Transaction transaction, BigDecimal billedAmount) {
        // Acquire pessimistic locks in consistent order to prevent deadlocks
        String firstLock = request.getSourceAccountNumber().compareTo(request.getDestinationAccountNumber()) < 0
                ? request.getSourceAccountNumber()
                : request.getDestinationAccountNumber();
        String secondLock = firstLock.equals(request.getSourceAccountNumber())
                ? request.getDestinationAccountNumber()
                : request.getSourceAccountNumber();

        Account first = accountRepository.findByAccountNumberForUpdate(firstLock)
                .orElseThrow(() -> new AccountNotFoundException(firstLock));
        Account second = accountRepository.findByAccountNumberForUpdate(secondLock)
                .orElseThrow(() -> new AccountNotFoundException(secondLock));

        Account source = first.getAccountNumber().equals(request.getSourceAccountNumber()) ? first : second;
        Account destination = first.getAccountNumber().equals(request.getDestinationAccountNumber()) ? first : second;

        if (source.getBalance().compareTo(billedAmount) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Available: %.2f, Required: %.2f",
                            source.getBalance(), billedAmount));
        }

        source.setBalance(source.getBalance().subtract(billedAmount));
        destination.setBalance(destination.getBalance().add(request.getAmount()));

        accountRepository.save(source);
        accountRepository.save(destination);

        transaction.setStatus(TransactionStatus.SUCCESSFUL);
        transaction.setStatusMessage("Transfer completed successfully");
        transactionRepository.save(transaction);

        log.info("Transfer successful: {} from {} to {} amount {}",
                transaction.getTransactionReference(),
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber(),
                request.getAmount());
    }

    private void validateTransferRequest(TransferRequest request) {
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }
    }

    @Override
    @Transactional(readOnly = true)
//    PagedResponse<List<RequestResponse>> getRequestsByStatus(Status status, Integer page, Integer pageSize) {
    public PagedResponse<List<TransferResponse>> getTransactions(TransactionFilterRequest filter, Integer page, Integer pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
//        Page<Request> requestPage = requestRepository.findByStatus(status, pageable);


//        Pageable pageable = PageRequest.of(
//                filter.getPage(),
//                Math.min(filter.getSize(), 100),
//                Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Transaction> spec = Specification
                .where(TransactionSpecification.hasStatus(filter.getStatus()))
                .and(TransactionSpecification.hasAccountNumber(filter.getAccountNumber()))
                .and(TransactionSpecification.createdAfter(filter.getStartDate()))
                .and(TransactionSpecification.createdBefore(filter.getEndDate()));

//        return transactionRepository.findAll(spec, pageable)
//                .map(transactionMapper::toTransferResponse);

        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        List<TransferResponse> responses = transactionPage.getContent().stream()
                .map(this::mapToTransferResponse)
//                .map(transactionMapper::toTransferResponse);
                .collect(Collectors.toList());

        return AppUtil.buildPagedResponse(page, pageSize, transactionPage, responses);
    }

    public TransferResponse mapToTransferResponse(Transaction tx) {
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

    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryResponse getSummaryForDate(LocalDate date) {
        return summaryRepository.findBySummaryDate(date)
                .map(transactionMapper::toSummaryResponse)
                .orElseGet(() -> computeSummaryForDate(date));
    }

    @Override
    @Transactional
    public void processCommissions() {
        log.info("Starting commission processing job");
        List<Transaction> transactions = transactionRepository
                .findUnprocessedSuccessfulTransactions(TransactionStatus.SUCCESSFUL);

        int processed = 0;
        for (Transaction tx : transactions) {
            BigDecimal commission = feeCalculationService.calculateCommission(tx.getTransactionFee());
            tx.setCommission(commission);
            tx.setCommissionWorthy(commission.compareTo(BigDecimal.ZERO) > 0);
            tx.setCommissionProcessed(true);
            transactionRepository.save(tx);
            processed++;
        }
        log.info("Commission processing complete. Processed {} transactions", processed);
    }

    @Override
    @Transactional
    public void generateDailySummary(LocalDate date) {
        log.info("Generating transaction summary for date: {}", date);
        TransactionSummaryResponse computed = computeSummaryForDate(date);

        TransactionSummary summary = summaryRepository.findBySummaryDate(date)
                .orElseGet(() -> TransactionSummary.builder().summaryDate(date).build());

        summary.setTotalTransactions(computed.getTotalTransactions());
        summary.setSuccessfulTransactions(computed.getSuccessfulTransactions());
        summary.setFailedTransactions(computed.getFailedTransactions());
        summary.setInsufficientFundTransactions(computed.getInsufficientFundTransactions());
        summary.setTotalAmount(computed.getTotalAmount());
        summary.setTotalFees(computed.getTotalFees());
        summary.setTotalCommissions(computed.getTotalCommissions());
        summary.setCommissionWorthyCount(computed.getCommissionWorthyCount());

        summaryRepository.save(summary);
        log.info("Summary saved for date: {}", date);
    }

    private TransactionSummaryResponse computeSummaryForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Transaction> transactions = transactionRepository.findByDateRange(start, end);

        long successful = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESSFUL).count();
        long failed = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.FAILED).count();
        long insufficientFund = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.INSUFFICIENT_FUND).count();

        BigDecimal totalAmount = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESSFUL)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESSFUL)
                .map(Transaction::getTransactionFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommissions = transactions.stream()
                .filter(t -> Boolean.TRUE.equals(t.getCommissionWorthy()) && t.getCommission() != null)
                .map(Transaction::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long commissionWorthyCount = transactions.stream()
                .filter(t -> Boolean.TRUE.equals(t.getCommissionWorthy())).count();

        return TransactionSummaryResponse.builder()
                .summaryDate(date)
                .totalTransactions((long) transactions.size())
                .successfulTransactions(successful)
                .failedTransactions(failed)
                .insufficientFundTransactions(insufficientFund)
                .totalAmount(totalAmount)
                .totalFees(totalFees)
                .totalCommissions(totalCommissions)
                .commissionWorthyCount(commissionWorthyCount)
                .build();
    }
}
