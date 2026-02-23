package com.bank.transfer.app.dto;

import com.bank.transfer.enums.TransactionStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TransactionFilterRequest {

    private TransactionStatus status;
    private String accountNumber;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private int page = 0;
    private int size = 20;
}
