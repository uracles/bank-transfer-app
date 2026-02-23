package com.bank.transfer.app.service.impl;

import com.bank.transfer.app.service.FeeCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class FeeCalculationServiceImpl implements FeeCalculationService {

    private final BigDecimal feeRate;
    private final BigDecimal feeCap;
    private final BigDecimal commissionRate;

    public FeeCalculationServiceImpl(
            @Value("${app.transfer.fee-rate:0.005}") double feeRate,
            @Value("${app.transfer.fee-cap:100.0}") double feeCap,
            @Value("${app.transfer.commission-rate:0.20}") double commissionRate) {
        this.feeRate = BigDecimal.valueOf(feeRate);
        this.feeCap = BigDecimal.valueOf(feeCap);
        this.commissionRate = BigDecimal.valueOf(commissionRate);
    }

    @Override
    public BigDecimal calculateTransactionFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(feeRate).setScale(4, RoundingMode.HALF_UP);
        return fee.compareTo(feeCap) > 0 ? feeCap : fee;
    }

    @Override
    public BigDecimal calculateCommission(BigDecimal transactionFee) {
        return transactionFee.multiply(commissionRate).setScale(4, RoundingMode.HALF_UP);
    }
}
