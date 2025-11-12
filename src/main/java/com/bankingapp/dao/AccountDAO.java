package com.bankingapp.dao;

import com.bankingapp.model.Account;

import java.sql.SQLException;
import java.util.List;

public interface AccountDAO {
    Account createAccount(Account account) throws SQLException;

    Account getAccountById(int id) throws SQLException;

    List<Account> getAllAccounts() throws SQLException;

    boolean updateAccount(Account account) throws SQLException;

    boolean deleteAccount(int id) throws SQLException;
}
