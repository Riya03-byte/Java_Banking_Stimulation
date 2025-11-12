package com.bankingapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DepositTransaction extends Transaction {

    public DepositTransaction(int id, int accountId, BigDecimal amount, LocalDateTime timestamp) {
        super(id, accountId, amount, timestamp);
    }

    public DepositTransaction(int accountId, BigDecimal amount, LocalDateTime timestamp) {
        super(accountId, amount, timestamp);
    }

    @Override
    public String getType() {
        return "DEPOSIT";
    }
}
