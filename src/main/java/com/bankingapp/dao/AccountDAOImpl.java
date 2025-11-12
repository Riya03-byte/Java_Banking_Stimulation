package com.bankingapp.dao;

import com.bankingapp.model.Account;
import com.bankingapp.model.AccountType;
import com.bankingapp.util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    private static final String INSERT_ACCOUNT_SQL =
            "INSERT INTO accounts (owner_name, account_type, balance) VALUES (?, ?, ?)";
    private static final String SELECT_ACCOUNT_BY_ID_SQL =
            "SELECT id, owner_name, account_type, balance FROM accounts WHERE id = ?";
    private static final String SELECT_ALL_ACCOUNTS_SQL =
            "SELECT id, owner_name, account_type, balance FROM accounts";
    private static final String UPDATE_ACCOUNT_SQL =
            "UPDATE accounts SET owner_name = ?, account_type = ?, balance = ? WHERE id = ?";
    private static final String DELETE_ACCOUNT_SQL =
            "DELETE FROM accounts WHERE id = ?";

    @Override
    public Account createAccount(Account account) throws SQLException {
        if (account == null) throw new IllegalArgumentException("Account cannot be null");
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_ACCOUNT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getOwnerName());
            ps.setString(2, account.getAccountType().name());
            ps.setBigDecimal(3, account.getBalance());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating account failed, no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }
            return account;
        }
    }

    @Override
    public Account getAccountById(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Account id must be positive");
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ACCOUNT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public List<Account> getAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_ACCOUNTS_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        }
        return accounts;
    }

    @Override
    public boolean updateAccount(Account account) throws SQLException {
        if (account == null) throw new IllegalArgumentException("Account cannot be null");
        if (account.getId() <= 0) throw new IllegalArgumentException("Account id must be positive");
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_ACCOUNT_SQL)) {
            ps.setString(1, account.getOwnerName());
            ps.setString(2, account.getAccountType().name());
            ps.setBigDecimal(3, account.getBalance());
            ps.setInt(4, account.getId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean deleteAccount(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Account id must be positive");
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_ACCOUNT_SQL)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String ownerName = rs.getString("owner_name");
        String accTypeStr = rs.getString("account_type");
        AccountType accountType;
        try {
            accountType = AccountType.valueOf(accTypeStr);
        } catch (IllegalArgumentException e) {
            accountType = AccountType.SAVINGS; // fallback default
        }
        BigDecimal balance = rs.getBigDecimal("balance");
        return new Account(id, ownerName, accountType, balance);
    }
}
