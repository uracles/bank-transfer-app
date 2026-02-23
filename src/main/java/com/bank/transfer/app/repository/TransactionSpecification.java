package com.bank.transfer.app.repository;

import com.bank.transfer.app.entity.Transaction;
import com.bank.transfer.app.enums.TransactionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> hasAccountNumber(String accountNumber) {
        return (root, query, cb) -> {
            if (accountNumber == null || accountNumber.isBlank()) return null;
            return cb.or(
                    cb.equal(root.get("sourceAccountNumber"), accountNumber),
                    cb.equal(root.get("destinationAccountNumber"), accountNumber)
            );
        };
    }

    public static Specification<Transaction> createdAfter(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return null;
            LocalDateTime start = startDate.atStartOfDay();
            return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
        };
    }

    public static Specification<Transaction> createdBefore(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return null;
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            return cb.lessThan(root.get("createdAt"), end);
        };
    }
}
