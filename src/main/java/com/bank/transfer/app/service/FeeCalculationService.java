package com.bank.transfer.app.service;

import java.math.BigDecimal;

public interface FeeCalculationService {

    /**
     * Calculate transaction fee: 0.5% of amount, capped at max fee.
     */
    BigDecimal calculateTransactionFee(BigDecimal amount);

    /**
     * Calculate commission: 20% of transaction fee.
     */
    BigDecimal calculateCommission(BigDecimal transactionFee);
}
