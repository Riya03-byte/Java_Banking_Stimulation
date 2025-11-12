package com.bankingapp.manager;

import com.bankingapp.dao.AccountDAO;
import com.bankingapp.dao.TransactionDAO;
import com.bankingapp.exceptions.AccountNotFoundException;
import com.bankingapp.exceptions.InsufficientFundsException;
import com.bankingapp.model.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AccountManager {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public AccountManager(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    public Account createAccount(String ownerName, AccountType type) throws SQLException {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name must not be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Account type must not be null");
        }
        Account account = new Account(ownerName.trim(), type);
        return accountDAO.createAccount(account);
    }

    public void deposit(int accountId, BigDecimal amount) throws SQLException, AccountNotFoundException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        Account account = accountDAO.getAccountById(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found.");
        }
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        boolean updated = accountDAO.updateAccount(account);
        if (!updated) {
            throw new SQLException("Failed to update account balance for deposit.");
        }
        DepositTransaction depositTransaction = new DepositTransaction(accountId, amount, LocalDateTime.now());
        transactionDAO.createTransaction(depositTransaction);
    }

    public void withdraw(int accountId, BigDecimal amount) throws SQLException, AccountNotFoundException, InsufficientFundsException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        Account account = accountDAO.getAccountById(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found.");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account ID " + accountId);
        }
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        boolean updated = accountDAO.updateAccount(account);
        if (!updated) {
            throw new SQLException("Failed to update account balance for withdrawal.");
        }
        WithdrawalTransaction withdrawalTransaction = new WithdrawalTransaction(accountId, amount, LocalDateTime.now());
        transactionDAO.createTransaction(withdrawalTransaction);
    }

    public void transfer(int fromAccountId, int toAccountId, BigDecimal amount) throws SQLException, AccountNotFoundException, InsufficientFundsException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        Account fromAccount = accountDAO.getAccountById(fromAccountId);
        if (fromAccount == null) {
            throw new AccountNotFoundException("Source account with ID " + fromAccountId + " not found.");
        }
        Account toAccount = accountDAO.getAccountById(toAccountId);
        if (toAccount == null) {
            throw new AccountNotFoundException("Destination account with ID " + toAccountId + " not found.");
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account ID " + fromAccountId);
        }

        // Begin transaction block
        try (var connection = accountDAO instanceof com.bankingapp.dao.AccountDAOImpl ?
                com.bankingapp.util.DBConnectionUtil.getConnection() : null) {
            if (connection != null) {
                connection.setAutoCommit(false);
            }
            // Withdraw from source
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            boolean updatedFrom = accountDAO.updateAccount(fromAccount);
            if (!updatedFrom) {
                if (connection != null) connection.rollback();
                throw new SQLException("Failed to update source account during transfer.");
            }
            // Deposit to destination
            toAccount.setBalance(toAccount.getBalance().add(amount));
            boolean updatedTo = accountDAO.updateAccount(toAccount);
            if (!updatedTo) {
                if (connection != null) connection.rollback();
                throw new SQLException("Failed to update destination account during transfer.");
            }
            // Create transaction record
            TransferTransaction transferTransaction = new TransferTransaction(fromAccountId, toAccountId, amount, LocalDateTime.now());
            transactionDAO.createTransaction(transferTransaction);

            if (connection != null) {
                connection.commit();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public List<Account> getAllAccounts() throws SQLException {
        return accountDAO.getAllAccounts();
    }
}
