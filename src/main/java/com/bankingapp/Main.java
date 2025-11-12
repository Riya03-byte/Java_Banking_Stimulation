package com.bankingapp;

import com.bankingapp.dao.AccountDAO;
import com.bankingapp.dao.AccountDAOImpl;
import com.bankingapp.dao.TransactionDAO;
import com.bankingapp.dao.TransactionDAOImpl;
import com.bankingapp.exceptions.AccountNotFoundException;
import com.bankingapp.exceptions.InsufficientFundsException;
import com.bankingapp.manager.AccountManager;
import com.bankingapp.model.Account;
import com.bankingapp.model.AccountType;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AccountDAO accountDAO = new AccountDAOImpl();
    private static final TransactionDAO transactionDAO = new TransactionDAOImpl();
    private static final AccountManager accountManager = new AccountManager(accountDAO, transactionDAO);

    public static void main(String[] args) {
        System.out.println("Welcome to the Banking Application!");

        boolean exit = false;
        while (!exit) {
            printMenu();
            int choice = readIntInput("Enter your choice: ");
            try {
                switch (choice) {
                    case 1 -> createAccountFlow();
                    case 2 -> depositFlow();
                    case 3 -> withdrawFlow();
                    case 4 -> transferFlow();
                    case 5 -> showAllAccountsFlow();
                    case 6 -> {
                        exit = true;
                        System.out.println("Exiting application. Goodbye!");
                    }
                    default -> System.out.println("Invalid option. Please enter a number between 1 and 6.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            } catch (AccountNotFoundException | InsufficientFundsException | IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("-------------------------------------------------");
        System.out.println("1) Create Account");
        System.out.println("2) Deposit");
        System.out.println("3) Withdraw");
        System.out.println("4) Transfer");
        System.out.println("5) Show All Accounts");
        System.out.println("6) Exit");
        System.out.println("-------------------------------------------------");
    }

    private static void createAccountFlow() throws SQLException {
        System.out.println("== Create Account ==");
        String ownerName = readStringInput("Enter holder name: ");
        AccountType accountType = readAccountTypeInput();
        Account account = accountManager.createAccount(ownerName, accountType);
        System.out.println("Account created successfully:");
        System.out.println(account);
    }

    private static void depositFlow() throws SQLException, AccountNotFoundException {
        System.out.println("== Deposit ==");
        int accountId = readIntInput("Enter account ID to deposit into: ");
        BigDecimal amount = readBigDecimalInput("Enter deposit amount: ");
        accountManager.deposit(accountId, amount);
        Account updated = accountDAO.getAccountById(accountId);
        System.out.println("Deposit successful. Updated account:");
        System.out.println(updated);
    }

    private static void withdrawFlow() throws SQLException, AccountNotFoundException, InsufficientFundsException {
        System.out.println("== Withdraw ==");
        int accountId = readIntInput("Enter account ID to withdraw from: ");
        BigDecimal amount = readBigDecimalInput("Enter withdrawal amount: ");
        accountManager.withdraw(accountId, amount);
        Account updated = accountDAO.getAccountById(accountId);
        System.out.println("Withdrawal successful. Updated account:");
        System.out.println(updated);
    }

    private static void transferFlow() throws SQLException, AccountNotFoundException, InsufficientFundsException {
        System.out.println("== Transfer ==");
        int fromAccountId = readIntInput("Enter source account ID: ");
        int toAccountId = readIntInput("Enter destination account ID: ");
        BigDecimal amount = readBigDecimalInput("Enter transfer amount: ");
        accountManager.transfer(fromAccountId, toAccountId, amount);
        Account fromUpdated = accountDAO.getAccountById(fromAccountId);
        Account toUpdated = accountDAO.getAccountById(toAccountId);
        System.out.println("Transfer successful. Updated accounts:");
        System.out.println("Source: " + fromUpdated);
        System.out.println("Destination: " + toUpdated);
    }

    private static void showAllAccountsFlow() throws SQLException {
        System.out.println("== All Accounts ==");
        List<Account> accounts = accountManager.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            accounts.forEach(System.out::println);
        }
    }

    private static int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine();
                int value = Integer.parseInt(line.trim());
                if (value < 0) {
                    System.out.println("Please enter a non-negative integer.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    private static BigDecimal readBigDecimalInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine();
                BigDecimal val = new BigDecimal(line.trim());
                if (val.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Please enter a positive amount.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    private static String readStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (input == null || input.trim().isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            } else {
                return input.trim();
            }
        }
    }

    private static AccountType readAccountTypeInput() {
        while (true) {
            System.out.print("Enter account type (SAVINGS, CHECKING, BUSINESS): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return AccountType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid account type. Please enter one of: SAVINGS, CHECKING, BUSINESS.");
            }
        }
    }
}
