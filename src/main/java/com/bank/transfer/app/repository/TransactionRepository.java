package com.bank.transfer.app.repository;

import com.bank.transfer.app.entity.Transaction;
import com.bank.transfer.app.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    boolean existsByTransactionReference(String transactionReference);

    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND " +
           "(t.commissionProcessed IS NULL OR t.commissionProcessed = false)")
    List<Transaction> findUnprocessedSuccessfulTransactions(@Param("status") TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt >= :startDate AND t.createdAt < :endDate")
    List<Transaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createdAt >= :startDate AND t.createdAt < :endDate AND t.status = :status")
    Long countByDateRangeAndStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") TransactionStatus status);
}
