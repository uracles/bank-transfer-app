package com.bank.transfer.app.service;

import com.bank.transfer.app.config.TransactionMapper;
import com.bank.transfer.app.dto.TransferRequest;
import com.bank.transfer.app.dto.TransferResponse;
import com.bank.transfer.app.entity.Account;
import com.bank.transfer.app.entity.Transaction;
import com.bank.transfer.app.enums.TransactionStatus;
import com.bank.transfer.app.exception.AccountNotFoundException;
import com.bank.transfer.app.exception.InsufficientFundsException;
import com.bank.transfer.app.repository.AccountRepository;
import com.bank.transfer.app.repository.TransactionRepository;
import com.bank.transfer.app.repository.TransactionSummaryRepository;
import com.bank.transfer.app.service.impl.FeeCalculationServiceImpl;
import com.bank.transfer.app.util.ReferenceGeneratorUtil;
import com.bank.transfer.app.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionSummaryRepository summaryRepository;

    @Mock
    private TransactionMapper transactionMapper;

    private TransferService transferService;

    private Account sourceAccount;
    private Account destAccount;

    @BeforeEach
    void setUp() {
        FeeCalculationService feeService = new FeeCalculationServiceImpl(0.005, 100.0, 0.20);

        transferService = new TransferServiceImpl(
                transactionRepository, accountRepository, summaryRepository,
                feeService, transactionMapper);

        sourceAccount = Account.builder()
                .id(1L)
                .accountNumber("0123456789")
                .accountName("Alice")
                .balance(new BigDecimal("100000.00"))
                .build();

        destAccount = Account.builder()
                .id(2L)
                .accountNumber("0987654321")
                .accountName("Bob")
                .balance(new BigDecimal("50000.00"))
                .build();
    }

    @Test
    @DisplayName("Should successfully process a valid transfer")
    void processTransfer_success() {
        TransferRequest request = buildRequest("0123456789", "0987654321", "5000.00");
        TransferResponse expected = TransferResponse.builder()
                .status(TransactionStatus.SUCCESSFUL).build();

        when(accountRepository.findByAccountNumberForUpdate("0123456789"))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate("0987654321"))
                .thenReturn(Optional.of(destAccount));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionMapper.toTransferResponse(any())).thenReturn(expected);

        TransferResponse response = transferService.processTransfer(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESSFUL);

        // Verify source account was debited
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).save(accountCaptor.capture());
        Account savedSource = accountCaptor.getAllValues().stream()
                .filter(a -> a.getAccountNumber().equals("0123456789")).findFirst().orElseThrow();
        // 5000 + (5000 * 0.005) = 5025
        assertThat(savedSource.getBalance()).isEqualByComparingTo(new BigDecimal("94975.0000"));
    }

    @Test
    @DisplayName("Should throw InsufficientFundsException when balance is low")
    void processTransfer_insufficientFunds() {
        sourceAccount.setBalance(new BigDecimal("10.00"));
        TransferRequest request = buildRequest("0123456789", "0987654321", "5000.00");

        when(accountRepository.findByAccountNumberForUpdate("0123456789"))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate("0987654321"))
                .thenReturn(Optional.of(destAccount));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> transferService.processTransfer(request))
                .isInstanceOf(InsufficientFundsException.class);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeastOnce()).save(txCaptor.capture());
        Transaction saved = txCaptor.getAllValues().stream()
                .filter(t -> t.getStatus() == TransactionStatus.INSUFFICIENT_FUND)
                .findFirst().orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.INSUFFICIENT_FUND);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException for unknown account")
    void processTransfer_accountNotFound() {
        TransferRequest request = buildRequest("0000000000", "0987654321", "1000.00");

        when(accountRepository.findByAccountNumberForUpdate(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.processTransfer(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("Should reject transfer to the same account")
    void processTransfer_sameAccount() {
        TransferRequest request = buildRequest("0123456789", "0123456789", "500.00");

        assertThatThrownBy(() -> transferService.processTransfer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same");
    }

    @Test
    @DisplayName("Should correctly debit fee from source and credit full amount to destination")
    void processTransfer_correctAmounts() {
        TransferRequest request = buildRequest("0123456789", "0987654321", "10000.00");

        when(accountRepository.findByAccountNumberForUpdate("0123456789"))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate("0987654321"))
                .thenReturn(Optional.of(destAccount));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionMapper.toTransferResponse(any())).thenReturn(new TransferResponse());

        transferService.processTransfer(request);

        // fee = 10000 * 0.005 = 50; source debited 10050
        assertThat(sourceAccount.getBalance()).isEqualByComparingTo(new BigDecimal("89950.0000"));
        // destination credited full 10000
        assertThat(destAccount.getBalance()).isEqualByComparingTo(new BigDecimal("60000.00"));
    }

    private TransferRequest buildRequest(String src, String dst, String amount) {
        return TransferRequest.builder()
                .sourceAccountNumber(src)
                .destinationAccountNumber(dst)
                .amount(new BigDecimal(amount))
                .description("Test transfer")
                .build();
    }
}
