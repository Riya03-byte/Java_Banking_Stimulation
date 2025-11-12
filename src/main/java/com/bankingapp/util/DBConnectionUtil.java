package com.bankingapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionUtil {
    private static final String PROPERTIES_FILE = "/config.properties";
    private static String jdbcUrl;
    private static String jdbcUsername;
    private static String jdbcPassword;

    static {
        try (InputStream input = DBConnectionUtil.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + PROPERTIES_FILE);
            }
            Properties prop = new Properties();
            prop.load(input);

            jdbcUrl = prop.getProperty("jdbc.url");
            jdbcUsername = prop.getProperty("jdbc.username");
            jdbcPassword = prop.getProperty("jdbc.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Error loading DB config: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }
}
