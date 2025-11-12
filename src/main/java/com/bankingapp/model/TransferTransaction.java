package com.bankingapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferTransaction extends Transaction {
    private int destinationAccountId;

    public TransferTransaction(int id, int accountId, int destinationAccountId, BigDecimal amount, LocalDateTime timestamp) {
        super(id, accountId, amount, timestamp);
        this.destinationAccountId = destinationAccountId;
    }

    public TransferTransaction(int accountId, int destinationAccountId, BigDecimal amount, LocalDateTime timestamp) {
        super(accountId, amount, timestamp);
        this.destinationAccountId = destinationAccountId;
    }

    public int getDestinationAccountId() {
        return destinationAccountId;
    }

    @Override
    public String getType() {
        return "TRANSFER";
    }
}
