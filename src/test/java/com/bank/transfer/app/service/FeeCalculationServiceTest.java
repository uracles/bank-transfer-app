package com.bank.transfer.app.service;

import com.bank.transfer.app.service.impl.FeeCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FeeCalculationServiceTest {

    private FeeCalculationService feeCalculationService;

    @BeforeEach
    void setUp() {
        feeCalculationService = new FeeCalculationServiceImpl(0.005, 100.0, 0.20);
    }

    @Test
    @DisplayName("Should calculate 0.5% fee for small amounts")
    void calculateFee_smallAmount() {
        BigDecimal fee = feeCalculationService.calculateTransactionFee(new BigDecimal("1000"));
        assertThat(fee).isEqualByComparingTo(new BigDecimal("5.0000"));
    }

    @Test
    @DisplayName("Should cap fee at 100 for large amounts")
    void calculateFee_largeAmountCapped() {
        BigDecimal fee = feeCalculationService.calculateTransactionFee(new BigDecimal("50000"));
        assertThat(fee).isEqualByComparingTo(new BigDecimal("100.0000"));
    }

    @Test
    @DisplayName("Should apply cap exactly at threshold (20000)")
    void calculateFee_atCapThreshold() {
        BigDecimal fee = feeCalculationService.calculateTransactionFee(new BigDecimal("20000"));
        assertThat(fee).isEqualByComparingTo(new BigDecimal("100.0000"));
    }

    @Test
    @DisplayName("Should calculate 20% commission on transaction fee")
    void calculateCommission() {
        BigDecimal commission = feeCalculationService.calculateCommission(new BigDecimal("100.00"));
        assertThat(commission).isEqualByComparingTo(new BigDecimal("20.0000"));
    }

    @ParameterizedTest(name = "Amount {0} -> Fee {1}")
    @CsvSource({
        "100, 0.5000",
        "1000, 5.0000",
        "10000, 50.0000",
        "20000, 100.0000",
        "100000, 100.0000"
    })
    @DisplayName("Parameterized fee calculation tests")
    void calculateFee_parameterized(String amount, String expectedFee) {
        BigDecimal fee = feeCalculationService.calculateTransactionFee(new BigDecimal(amount));
        assertThat(fee).isEqualByComparingTo(new BigDecimal(expectedFee));
    }
}
