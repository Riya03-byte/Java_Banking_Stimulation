package com.bankingapp.dao;

import com.bankingapp.model.*;
import com.bankingapp.util.DBConnectionUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    // destination_account_id column is optional in some DB schemas. Avoid referencing it directly
    // to prevent errors when the column is not present. If you rely on storing destination
    // account for transfers, update the DB schema accordingly.
    private static final String INSERT_TRANSACTION_SQL = "INSERT INTO transactions (account_id, type, amount, timestamp) VALUES (?, ?, ?, ?)";
    // Use wildcard select and map columns defensively to support varying DB schemas
    private static final String SELECT_TRANSACTIONS_BY_ACCOUNT_SQL = "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
    private static final String SELECT_ALL_TRANSACTIONS_SQL = "SELECT * FROM transactions ORDER BY timestamp DESC";

    @Override
    public Transaction createTransaction(Transaction transaction) throws SQLException {
        if (transaction == null) throw new IllegalArgumentException("Transaction cannot be null");

        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_TRANSACTION_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getAccountId());
            ps.setString(2, transaction.getType());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(transaction.getTimestamp()));
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
        ResultSetMetaData md = rs.getMetaData();

        // helper to check presence
        java.util.function.Predicate<String> hasColumn = name -> {
            try {
                int cols = md.getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    if (md.getColumnLabel(i).equalsIgnoreCase(name) || md.getColumnName(i).equalsIgnoreCase(name)) return true;
                }
            } catch (SQLException ignored) {}
            return false;
        };

        // helper to read int from multiple possible column names
        java.util.function.Function<String[], Integer> getIntFrom = names -> {
            for (String n : names) {
                if (hasColumn.test(n)) {
                    try { return rs.getInt(n); } catch (SQLException ignored) {}
                }
            }
            return -1;
        };

        java.util.function.Function<String[], java.math.BigDecimal> getBigDecimalFrom = names -> {
            for (String n : names) {
                if (hasColumn.test(n)) {
                    try { return rs.getBigDecimal(n); } catch (SQLException ignored) {}
                }
            }
            return java.math.BigDecimal.ZERO;
        };

        java.util.function.Function<String[], Timestamp> getTimestampFrom = names -> {
            for (String n : names) {
                if (hasColumn.test(n)) {
                    try { return rs.getTimestamp(n); } catch (SQLException ignored) {}
                }
            }
            return null;
        };

        int id = getIntFrom.apply(new String[] {"id", "transaction_id", "txn_id"});
        int accountId = getIntFrom.apply(new String[] {"account_id", "acct_id", "accountid"});
        String type = "";
        if (hasColumn.test("type")) type = rs.getString("type");
        else if (hasColumn.test("transaction_type")) type = rs.getString("transaction_type");
        else type = rs.getString( md.getColumnLabel(3) ); // best effort fallback

        java.math.BigDecimal amount = getBigDecimalFrom.apply(new String[] {"amount", "value", "tx_amount"});
        Timestamp ts = getTimestampFrom.apply(new String[] {"timestamp", "ts", "created_at", "datetime"});
        LocalDateTime timestamp = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        int destAccountId = getIntFrom.apply(new String[] {"destination_account_id", "dest_account_id", "to_account_id"});

        switch (type.toUpperCase()) {
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
