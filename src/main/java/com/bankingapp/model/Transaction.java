package com.bankingapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Transaction {
    protected int id;
    protected int accountId;
    protected BigDecimal amount;
    protected LocalDateTime timestamp;

    public Transaction(int id, int accountId, BigDecimal amount, LocalDateTime timestamp) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public Transaction(int accountId, BigDecimal amount, LocalDateTime timestamp) {
        this(0, accountId, amount, timestamp);
    }

    public int getId() {
        return id;
    }

    public int getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public abstract String getType();

    public void setId(int id) {
        this.id = id;
    }
}
