package com.bankingapp.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {
    private int id;
    private String ownerName;
    private AccountType accountType;
    private BigDecimal balance;

    public Account() {
        this.balance = BigDecimal.ZERO;
    }

    public Account(int id, String ownerName, AccountType accountType, BigDecimal balance) {
        this.id = id;
        this.ownerName = ownerName;
        this.accountType = accountType;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    public Account(String ownerName, AccountType accountType) {
        this.ownerName = ownerName;
        this.accountType = accountType;
        this.balance = BigDecimal.ZERO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", ownerName='" + ownerName + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return id == account.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
