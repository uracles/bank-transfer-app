package com.bank.transfer.app.config;

import com.bank.transfer.app.entity.Account;
import com.bank.transfer.app.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) {
        if (accountRepository.count() > 0) {
            return;
        }

        List<Account> accounts = List.of(
                Account.builder()
                        .accountNumber("0123456789")
                        .accountName("Peter Obi")
                        .balance(new BigDecimal("500000.00"))
                        .build(),

                Account.builder()
                        .accountNumber("0987654321")
                        .accountName("Aisha Bello")
                        .balance(new BigDecimal("250000.00"))
                        .build(),

                Account.builder()
                        .accountNumber("1122334455")
                        .accountName("Asiwaju Tinubu")
                        .balance(new BigDecimal("1000000.00"))
                        .build(),

                Account.builder()
                        .accountNumber("5566778899")
                        .accountName("Emeka Nwosu")
                        .balance(new BigDecimal("50000.00"))
                        .build(),

                Account.builder()
                        .accountNumber("9988776655")
                        .accountName("Fatima Yusuf")
                        .balance(new BigDecimal("5000.00"))
                        .build()
        );

        accountRepository.saveAll(accounts);
        log.info("Seeded {} Nigerian test accounts", accounts.size());
    }
}