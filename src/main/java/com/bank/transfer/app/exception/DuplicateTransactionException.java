package com.bank.transfer.app.exception;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String reference) {
        super("Duplicate transaction reference: " + reference);
    }
}
