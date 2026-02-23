package com.bank.transfer.app.repository;

import com.bank.transfer.app.entity.TransactionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TransactionSummaryRepository extends JpaRepository<TransactionSummary, Long> {

    Optional<TransactionSummary> findBySummaryDate(LocalDate summaryDate);
}
