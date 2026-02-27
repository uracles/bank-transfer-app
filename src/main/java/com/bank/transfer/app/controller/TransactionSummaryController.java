package com.bank.transfer.app.controller;

import com.bank.transfer.app.util.ApiResponse;
import com.bank.transfer.app.dto.TransactionSummaryResponse;
import com.bank.transfer.app.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/summaries")
@RequiredArgsConstructor
@Tag(name = "Transaction Summary API", description = "Daily transaction summaries")
public class TransactionSummaryController {

    private final TransferService transferService;

    @GetMapping("/daily")
    @Operation(summary = "Get transaction summary for a specific date (defaults to today)")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getDailySummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();

        if (targetDate.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Summary date cannot be in the future"));
        }

        TransactionSummaryResponse summary = transferService.getSummaryForDate(targetDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/generate")
    @Operation(summary = "Manually trigger daily summary generation for a specific date")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> generateSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now().minusDays(1);

        if (targetDate.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Cannot generate summary for a future date"));
        }

        transferService.generateDailySummary(targetDate);
        TransactionSummaryResponse summary = transferService.getSummaryForDate(targetDate);
        return ResponseEntity.ok(ApiResponse.success("Summary generated for " + targetDate, summary));
    }
}
