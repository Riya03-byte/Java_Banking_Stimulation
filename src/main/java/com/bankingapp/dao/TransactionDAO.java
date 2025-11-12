package com.bankingapp.dao;

import com.bankingapp.model.Transaction;

import java.sql.SQLException;
import java.util.List;

public interface TransactionDAO {
    Transaction createTransaction(Transaction transaction) throws SQLException;

    List<Transaction> getTransactionsByAccountId(int accountId) throws SQLException;

    List<Transaction> getAllTransactions() throws SQLException;
}
