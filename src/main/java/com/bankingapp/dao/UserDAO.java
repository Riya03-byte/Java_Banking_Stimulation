package com.bankingapp.dao;

import com.bankingapp.model.User;

import java.sql.SQLException;

public interface UserDAO {
    User createUser(User user) throws SQLException;

    User getUserByUsername(String username) throws SQLException;
}
