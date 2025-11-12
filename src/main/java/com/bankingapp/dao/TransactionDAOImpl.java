package com.bankingapp.dao;

import com.bankingapp.model.*;
import com.bankingapp.util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    private static final String INSERT_TRANSACTION_SQL = "INSERT INTO transactions (account_id, type, amount, timestamp, destination_account_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_TRANSACTIONS_BY_ACCOUNT_SQL = "SELECT id, account_id, type, amount, timestamp, destination_account_id FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
    private static final String SELECT_ALL_TRANSACTIONS_SQL = "SELECT id, account_id, type, amount, timestamp, destination_account_id FROM transactions ORDER BY timestamp DESC";

    @Override
    public Transaction createTransaction(Transaction transaction) throws SQLException {
        if (transaction == null) throw new IllegalArgumentException("Transaction cannot be null");

        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_TRANSACTION_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getAccountId());
            ps.setString(2, transaction.getType());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(transaction.getTimestamp()));
            if (transaction instanceof TransferTransaction) {
                ps.setInt(5, ((TransferTransaction) transaction).getDestinationAccountId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
            return transaction;
        }
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(int accountId) throws SQLException {
        if (accountId <= 0) throw new IllegalArgumentException("Account id must be positive");
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_TRANSACTIONS_BY_ACCOUNT_SQL)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_TRANSACTIONS_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int accountId = rs.getInt("account_id");
        String type = rs.getString("type");
        BigDecimal amount = rs.getBigDecimal("amount");
        Timestamp ts = rs.getTimestamp("timestamp");
        LocalDateTime timestamp = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        int destAccountId = rs.getInt("destination_account_id");
        if (rs.wasNull()) {
            destAccountId = -1;
        }

        switch (type) {
            case "DEPOSIT":
                return new DepositTransaction(id, accountId, amount, timestamp);
            case "WITHDRAWAL":
                return new WithdrawalTransaction(id, accountId, amount, timestamp);
            case "TRANSFER":
                return new TransferTransaction(id, accountId, destAccountId, amount, timestamp);
            default:
                throw new SQLException("Unknown transaction type: " + type);
        }
    }
}
