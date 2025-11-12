package com.bankingapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WithdrawalTransaction extends Transaction {

    public WithdrawalTransaction(int id, int accountId, BigDecimal amount, LocalDateTime timestamp) {
        super(id, accountId, amount, timestamp);
    }

    public WithdrawalTransaction(int accountId, BigDecimal amount, LocalDateTime timestamp) {
        super(accountId, amount, timestamp);
    }

    @Override
    public String getType() {
        return "WITHDRAWAL";
    }
}
