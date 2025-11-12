package com.bankingapp;

import com.bankingapp.dao.AccountDAO;
import com.bankingapp.dao.AccountDAOImpl;
import com.bankingapp.dao.TransactionDAO;
import com.bankingapp.dao.TransactionDAOImpl;
import com.bankingapp.dao.UserDAO;
import com.bankingapp.dao.UserDAOImpl;
import com.bankingapp.model.User;
import com.bankingapp.util.PasswordUtil;
import com.bankingapp.exceptions.AccountNotFoundException;
import com.bankingapp.exceptions.InsufficientFundsException;
import com.bankingapp.manager.AccountManager;
import com.bankingapp.model.Account;
import com.bankingapp.model.AccountType;
import com.bankingapp.email.ConsoleEmailService;
import com.bankingapp.model.Transaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AccountDAO accountDAO = new AccountDAOImpl();
    private static final TransactionDAO transactionDAO = new TransactionDAOImpl();
    private static final AccountManager accountManager = new AccountManager(accountDAO, transactionDAO);
    private static final UserDAO userDAO = new UserDAOImpl();
    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("Welcome to the Banking Application!");

        boolean exitApp = false;
        while (!exitApp) {
            if (currentUser == null) {
                int choice = showAuthMenuAndReadChoice();
                switch (choice) {
                    case 1 -> signUpFlow();
                    case 2 -> signInFlow();
                    case 3 -> exitApp = true;
                    default -> System.out.println("Invalid option. Please enter a number between 1 and 3.");
                }
            } else {
                // Logged-in dashboard
                System.out.println("\nWelcome, " + currentUser.getFullName() + "!\n");
                printMenuLoggedIn();
                int choice = readIntInput("Enter your choice: ");
                try {
                    switch (choice) {
                        case 1 -> createAccountFlow();
                        case 2 -> showAllAccountsFlow();
                        case 3 -> depositFlow();
                        case 4 -> withdrawFlow();
                        case 5 -> transferFlow();
                        case 6 -> transactionHistoryFlow();
                        case 7 -> sendReportFlow();
                        case 8 -> {
                            // logout
                            currentUser = null;
                            System.out.println("You have been logged out.");
                        }
                        case 9 -> checkMinimumBalancesFlow();
                        default -> System.out.println("Invalid option. Please enter a number between 1 and 8.");
                    }
                } catch (SQLException e) {
                    System.err.println("Database error: " + e.getMessage());
                } catch (AccountNotFoundException | InsufficientFundsException | IllegalArgumentException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
            System.out.println();
        }
        scanner.close();
    }
    
    private static int showAuthMenuAndReadChoice() {
        System.out.println("-------------------------------------------------");
        System.out.println("1) Sign Up");
        System.out.println("2) Sign In");
        System.out.println("3) Exit");
        System.out.println("-------------------------------------------------");
        return readIntInput("Enter your choice: ");
    }
    
    private static void printMenuLoggedIn() {
        System.out.println("== User Dashboard ==");
        System.out.println("-------------------------------------------------");
        System.out.println("1) Create Account");
        System.out.println("2) Show All Accounts");
        System.out.println("3) Deposit");
        System.out.println("4) Withdraw");
        System.out.println("5) Transfer");
        System.out.println("6) Transaction History");
        System.out.println("7) Generate & Email Account Report");
        System.out.println("8) Logout");
        System.out.println("9) Check Minimum Balances (threshold = 1000)");
        System.out.println("-------------------------------------------------");
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

    

    private static void sendReportFlow() {
        System.out.println("== Generate & Email Account Report ==");
        try {
            int accountId = readIntInput("Enter account ID to generate report for: ");
            var reportGen = new com.bankingapp.reporting.ReportGenerator(transactionDAO);
            String report = reportGen.generateAccountStatement(accountId);
            // send via console email service to current user's username
            var emailSvc = new com.bankingapp.email.ConsoleEmailService();
            String to = currentUser != null ? currentUser.getUsername() : "unknown@local";
            emailSvc.sendEmail(to, "Account Statement for account " + accountId, report);
        } catch (Exception e) {
            System.err.println("Failed to generate or send report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void transactionHistoryFlow() throws SQLException {
        System.out.println("== Transaction History ==");
        int accountId = readIntInput("Enter account ID to view transactions for: ");
        java.util.List<Transaction> txs = transactionDAO.getTransactionsByAccountId(accountId);
        if (txs.isEmpty()) {
            System.out.println("No transactions found for account " + accountId);
            return;
        }
        System.out.println("Transactions for account " + accountId + ":");
        txs.forEach(t -> {
            String extra = "";
            if (t instanceof com.bankingapp.model.TransferTransaction) {
                int dest = ((com.bankingapp.model.TransferTransaction) t).getDestinationAccountId();
                extra = ", to=" + dest;
            }
            System.out.println(String.format("#%d %s %s%s @ %s", t.getId(), t.getType(), t.getAmount(), extra, t.getTimestamp()));
        });
    }

    private static void checkMinimumBalancesFlow() throws SQLException {
        final java.math.BigDecimal MIN = new java.math.BigDecimal("1000.00");
        System.out.println("== Check Minimum Balances ==");
        List<Account> accounts = accountManager.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        ConsoleEmailService emailSvc = new ConsoleEmailService();
        boolean anyBelow = false;
        for (Account a : accounts) {
            if (a.getBalance().compareTo(MIN) < 0) {
                anyBelow = true;
                String msg = String.format("ALERT: Account id=%d owner='%s' has low balance: %s (minimum %s)", a.getId(), a.getOwnerName(), a.getBalance(), MIN);
                System.out.println(msg);
                // send console alert to owner (owner name used as 'to' address placeholder)
                try {
                    emailSvc.sendEmail(a.getOwnerName(), "Low balance alert for account " + a.getId(), msg);
                } catch (Exception e) {
                    System.err.println("Failed to send alert for account " + a.getId() + ": " + e.getMessage());
                }
            }
        }
        if (!anyBelow) {
            System.out.println("All accounts meet the minimum balance of " + MIN);
        }
    }

    private static void signUpFlow() {
        System.out.println("== Sign Up ==");
        try {
            String username = readStringInput("Choose a username: ");
            String password = readPasswordHidden("Choose a password: ");
            String fullName = readStringInput("Enter your full name: ");
            if (userDAO.getUserByUsername(username) != null) {
                System.out.println("Username already exists. Please choose another.");
                return;
            }
            String hash = PasswordUtil.hash(password);
            var user = new com.bankingapp.model.User(username, hash, fullName);
            userDAO.createUser(user);
            System.out.println("Sign up successful. You can now sign in.");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void signInFlow() {
        System.out.println("== Sign In ==");
        try {
            String username = readStringInput("Username: ");
            String password = readPasswordHidden("Password: ");
            User user = userDAO.getUserByUsername(username);
            if (user == null) {
                System.out.println("Invalid username or password.");
                return;
            }
            String hash = PasswordUtil.hash(password);
            if (!hash.equals(user.getPasswordHash())) {
                System.out.println("Invalid username or password.");
                return;
            }
            currentUser = user;
            System.out.println("Sign in successful. Welcome, " + currentUser.getFullName() + "!");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static String readPasswordHidden(String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword(prompt);
            return pwd == null ? "" : new String(pwd);
        } else {
            // Console not available (IDE or redirected). Fall back to visible input with a warning.
            System.out.println("(Warning: password input will be visible in this environment)");
            return readStringInput(prompt);
        }
    }
}
