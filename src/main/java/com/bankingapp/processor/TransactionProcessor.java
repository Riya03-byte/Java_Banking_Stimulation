package com.bankingapp.processor;

import com.bankingapp.dao.AccountDAO;
import com.bankingapp.dao.TransactionDAO;
import com.bankingapp.manager.AccountManager;
import com.bankingapp.model.*;
import com.bankingapp.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Processes transactions (replays them through AccountManager) and logs failures to a transaction_errors table.
 */
public class TransactionProcessor {
    private final AccountManager accountManager;
    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;

    private static final String CREATE_ERRORS_TABLE = "CREATE TABLE IF NOT EXISTS transaction_errors (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "transaction_type VARCHAR(50), " +
            "details TEXT, " +
            "error_message TEXT, " +
            "timestamp DATETIME" +
            ")";

    private static final String INSERT_ERROR_SQL = "INSERT INTO transaction_errors (transaction_type, details, error_message, timestamp) VALUES (?, ?, ?, ?)";

    public TransactionProcessor(AccountManager accountManager, TransactionDAO transactionDAO, AccountDAO accountDAO) {
        this.accountManager = accountManager;
        this.transactionDAO = transactionDAO;
        this.accountDAO = accountDAO;
        try (Connection conn = DBConnectionUtil.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate(CREATE_ERRORS_TABLE);
        } catch (SQLException e) {
            System.err.println("Warning: could not create transaction_errors table: " + e.getMessage());
        }
    }

    public void processTransactions(List<Transaction> transactions) {
        for (Transaction t : transactions) {
            try {
                // route by type
                switch (t.getType()) {
                    case "DEPOSIT" -> {
                        accountManager.deposit(t.getAccountId(), t.getAmount());
                        transactionDAO.createTransaction(t);
                    }
                    case "WITHDRAWAL" -> {
                        accountManager.withdraw(t.getAccountId(), t.getAmount());
                        transactionDAO.createTransaction(t);
                    }
                    case "TRANSFER" -> {
                        if (t instanceof TransferTransaction) {
                            TransferTransaction tr = (TransferTransaction) t;
                            accountManager.transfer(tr.getAccountId(), tr.getDestinationAccountId(), tr.getAmount());
                            transactionDAO.createTransaction(tr);
                        } else {
                            throw new SQLException("Malformed transfer transaction object");
                        }
                    }
                    default -> throw new SQLException("Unknown transaction type: " + t.getType());
                }
            } catch (Exception e) {
                logError(t, e);
            }
        }
    }

    private void logError(Transaction t, Exception e) {
        String details = String.format("id=%d, account=%d, amount=%s, ts=%s", t.getId(), t.getAccountId(), t.getAmount(), t.getTimestamp());
        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(INSERT_ERROR_SQL)) {
            ps.setString(1, t.getType());
            ps.setString(2, details);
            ps.setString(3, e.getMessage());
            ps.setObject(4, LocalDateTime.now());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Failed to log transaction error: " + ex.getMessage());
        }
    }
}
