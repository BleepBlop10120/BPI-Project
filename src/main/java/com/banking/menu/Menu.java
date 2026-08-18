package com.banking.menu;

import com.banking.dao.AccountDAO;
import com.banking.dao.AuthDAO;
import com.banking.dao.TransactionDAO;
import com.banking.dao.impl.AccountDAOImpl;
import com.banking.dao.impl.AuthDAOImpl;
import com.banking.dao.impl.TransactionDAOImpl;
import com.banking.model.Account;
import com.banking.security.Pbkdf2PasswordHasher;
import com.banking.security.PasswordHasher;
import com.banking.services.AccountService;
import com.banking.services.AuthService;
import com.banking.services.TransactionService;
import com.banking.session.UserSession;

import java.sql.SQLException;
import java.util.Scanner;

public class Menu {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final AuthService authService;
    private final UserSession userSession;
    private final Scanner scanner;

    public Menu() {
        scanner = new Scanner(System.in);
        userSession = new UserSession();

        AccountDAO accountDAO = new AccountDAOImpl();
        TransactionDAO transactionDAO = new TransactionDAOImpl();
        AuthDAO authDAO = new AuthDAOImpl();
        PasswordHasher passwordHasher = new Pbkdf2PasswordHasher();

        accountService = new AccountService(accountDAO, authDAO, passwordHasher, scanner, userSession);
        transactionService = new TransactionService(
                accountDAO, transactionDAO, accountService, scanner, userSession
        );
        authService = new AuthService(
                authDAO, accountService, passwordHasher, scanner, userSession
        );
    }

    public void startMenu() throws SQLException {

        if (!handleLoginGate()) {
            return;
        }

        boolean running = true;

        while (running) {
            displayMenu();

            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> accountService.balanceInquiry();
                case "2" -> accountService.listAccounts();
                case "3" -> transactionService.deposit();
                case "4" -> transactionService.withdraw();
                case "5" -> transactionService.transfer();
                case "6" -> transactionService.viewTransactionHistory();
                case "7" -> transactionService.miniStatement();
                case "8" -> running = false;
                default -> System.out.println("Invalid input");
            }
        }

        userSession.logout();
        System.out.println("Goodbye!");
    }

    /**
     * Shows the pre-login screen (login / create account / exit).
     */
    private boolean handleLoginGate() {

        while (true) {
            System.out.println();
            System.out.println("==================================");
            System.out.println("=              BPI               =");
            System.out.println("==================================");
            System.out.println(" 1. Login");
            System.out.println(" 2. Create Account");
            System.out.println(" 3. Exit");
            System.out.println("==================================");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    Account account = authService.login();
                    if (account != null && userSession.isLoggedIn()) {
                        return true;
                    }
                }
                case "2" -> accountService.createAccount();
                case "3" -> {
                    System.out.println("Goodbye!");
                    return false;
                }
                default -> System.out.println("Invalid input");
            }
        }
    }

    private void displayMenu() {

        Account currentAccount = userSession.getCurrentAccount();

        System.out.println();
        System.out.println("==================================");
        System.out.println("=              BPI               =");
        System.out.println("==================================");
        System.out.println(" Logged in as: " + currentAccount.getAccountName()
                + " (" + currentAccount.getAccountNumber() + ")");
        System.out.println("----------------------------------");
        System.out.println(" 1. Balance Inquiry");
        System.out.println(" 2. List Accounts");
        System.out.println(" 3. Deposit");
        System.out.println(" 4. Withdraw");
        System.out.println(" 5. Transfer");
        System.out.println(" 6. Transaction History");
        System.out.println(" 7. Mini Statement");
        System.out.println(" 8. Exit");
        System.out.println("==================================");
    }
}
