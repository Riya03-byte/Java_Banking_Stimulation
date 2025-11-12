package com.bankingapp.dao;

import com.bankingapp.model.User;
import com.bankingapp.util.DBConnectionUtil;

import java.sql.*;

public class UserDAOImpl implements UserDAO {

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(100) NOT NULL UNIQUE, " +
            "password_hash VARCHAR(256) NOT NULL, " +
            "full_name VARCHAR(200)" +
            ")";

    private static final String INSERT_USER_SQL = "INSERT INTO users (username, password_hash, full_name) VALUES (?, ?, ?)";
    private static final String SELECT_BY_USERNAME_SQL = "SELECT id, username, password_hash, full_name FROM users WHERE username = ?";

    public UserDAOImpl() {
        // Ensure users table exists. Best effort: if DB permissions don't allow, operations will fail later.
        try (Connection conn = DBConnectionUtil.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            // Log/ignore here; higher-level calls will surface DB errors when needed.
            System.err.println("Warning: could not ensure users table exists: " + e.getMessage());
        }
    }

    @Override
    public User createUser(User user) throws SQLException {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Creating user failed, no rows affected.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        }
    }

    @Override
    public User getUserByUsername(String username) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USERNAME_SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String user = rs.getString("username");
                    String hash = rs.getString("password_hash");
                    String full = rs.getString("full_name");
                    return new User(id, user, hash, full);
                }
            }
        }
        return null;
    }
}
