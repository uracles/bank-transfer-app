package com.bank.transfer.app.controller;

import com.bank.transfer.app.util.ApiResponse;
import com.bank.transfer.app.dto.TransactionFilterRequest;
import com.bank.transfer.app.dto.TransferRequest;
import com.bank.transfer.app.dto.TransferResponse;
import com.bank.transfer.app.service.TransferService;
import com.bank.transfer.app.util.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfer API", description = "Money transfer operations")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/between-accounts")
    @Operation(summary = "Initiate a money transfer between two accounts")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.processTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer processed", response));
    }

    @GetMapping("/retrieve/transactions")
    @Operation(summary = "Retrieve paginated list of transactions with optional filters")
    public PagedResponse<List<TransferResponse>> getTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @ModelAttribute TransactionFilterRequest filter) {
        return transferService.getTransactions(filter, page, pageSize);
    }
}
