package com.bankingapp;

import com.bankingapp.dao.AccountDAOImpl;
import com.bankingapp.dao.TransactionDAOImpl;
import com.bankingapp.manager.AccountManager;
import com.bankingapp.model.Account;
import com.bankingapp.model.AccountType;

import java.math.BigDecimal;

public class Runner {

    public static void main(String[] args) {
        var accountDAO = new AccountDAOImpl();
        var transactionDAO = new TransactionDAOImpl();
        var manager = new AccountManager(accountDAO, transactionDAO);

        try {
            System.out.println("=== Runner: create account and deposit ===");
            Account acct = manager.createAccount("Runner User", AccountType.SAVINGS);
            System.out.println("Created account: " + acct);

            BigDecimal depositAmount = new BigDecimal("100.00");
            manager.deposit(acct.getId(), depositAmount);
            Account updated = accountDAO.getAccountById(acct.getId());
            System.out.println("After deposit: " + updated);
        } catch (Exception e) {
            System.err.println("Runner encountered an error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
