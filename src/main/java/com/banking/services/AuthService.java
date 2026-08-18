package com.banking.services;

import com.banking.dao.AuthDAO;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.AuthenticationException;
import com.banking.model.Account;
import com.banking.model.AccountCredential;
import com.banking.security.PasswordHasher;
import com.banking.session.UserSession;

import java.sql.SQLException;
import java.util.Scanner;

public class AuthService {

    private static final int MAX_ATTEMPTS = 3;

    private final AuthDAO authDAO;
    private final AccountService accountService;
    private final PasswordHasher passwordHasher;
    private final Scanner scanner;
    private final UserSession userSession;

    public AuthService(AuthDAO authDAO, AccountService accountService,
                       PasswordHasher passwordHasher, Scanner scanner,
                       UserSession userSession) {
        this.authDAO = authDAO;
        this.accountService = accountService;
        this.passwordHasher = passwordHasher;
        this.scanner = scanner;
        this.userSession = userSession;
    }

    /**
     * Prompts for username/password up to MAX_ATTEMPTS times.
     * On successful authentication, the account is stored in UserSession.
     * Returns the logged-in account, or null if all attempts failed.
     */
    public Account login() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            try {
                Account account = authenticate(username, password);
                userSession.login(account);

                System.out.println("\nWelcome, " + account.getAccountName() + "!\n");
                return account;
            } catch (AuthenticationException e) {
                int remaining = MAX_ATTEMPTS - attempt;
                System.out.println(e.getMessage() +
                        (remaining > 0 ? " (" + remaining + " attempt(s) left)\n" : "\n"));
            } catch (SQLException e) {
                System.out.println("Login failed: " + e.getMessage());
            }
        }
        System.out.println("Too many failed login attempts. Exiting.");
        return null;
    }

    private Account authenticate(String username, String password)
            throws AuthenticationException, SQLException {

        AccountCredential credential = authDAO.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password."));

        if (!passwordHasher.verify(password, credential.getSalt(), credential.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password.");
        }

        try {
            return accountService.findAccountOrThrow(credential.getAccountNumber());
        } catch (AccountNotFoundException e) {
            throw new AuthenticationException("Invalid username or password.");
        }
    }
}
