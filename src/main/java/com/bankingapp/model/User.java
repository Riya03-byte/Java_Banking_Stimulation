package com.bankingapp.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;

    public User(int id, String username, String passwordHash, String fullName) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    public User(String username, String passwordHash, String fullName) {
        this(0, username, passwordHash, fullName);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }
}
