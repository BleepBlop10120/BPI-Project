package com.banking.services;

import com.banking.dao.AccountDAO;
import com.banking.dao.AuthDAO;
import com.banking.exception.AccountNotFoundException;
import com.banking.model.Account;
import com.banking.model.AccountCredential;
import com.banking.security.PasswordHasher;
import com.banking.session.UserSession;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class AccountService {

    private final AccountDAO accountDAO;
    private final AuthDAO authDAO;
    private final PasswordHasher passwordHasher;
    private final Scanner scanner;
    private final UserSession userSession;


    public AccountService(AccountDAO accountDAO, AuthDAO authDAO, PasswordHasher passwordHasher, Scanner scanner, UserSession userSession) {
        this.accountDAO = accountDAO;
        this.authDAO = authDAO;
        this.passwordHasher = passwordHasher;
        this.scanner = scanner;
        this.userSession = userSession;
    }



    public void createAccount() {

        System.out.print("Enter your First Name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Enter your Last Name: ");
        String lastName = scanner.nextLine().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            System.out.println("\nFirst Name or Last Name is empty");
            return;
        }

        String name = firstName + " " + lastName;

        System.out.print("Choose a username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Choose a password: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username or password cannot be empty.");
            return;
        }

        System.out.print("Enter your Balance: ");
        BigDecimal balance = new BigDecimal(scanner.nextLine().trim());
        if  (balance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Balance is negative");
            return;
        }

        String accountNumber = generateAccountNumber();

        Account account = new Account(accountNumber,name ,balance);
        try {
            accountDAO.createAccount(account);
            System.out.println("\nAccount created successfully\n");
            printAccountSummary(account);

            String salt = passwordHasher.generateSalt();
            String hash = passwordHasher.hash(password, salt);
            authDAO.save(new AccountCredential(accountNumber, username, hash, salt));
            System.out.println("Login credentials created. You can now log in with username: " + username);

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    public void balanceInquiry() {
        System.out.println("\n------ BALANCE INQUIRY ------");

        try {
            if (!userSession.isLoggedIn()) {
                throw new IllegalStateException("No account is currently logged in.");
            }

            Account account = userSession.getCurrentAccount();

            Account latestAccount = findAccountOrThrow(account.getAccountNumber());

            account.setBalance(latestAccount.getBalance());
            account.setUpdatedAt(latestAccount.getUpdatedAt());

            System.out.println();
            printAccountSummary(account);

        } catch (IllegalStateException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (AccountNotFoundException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    public void listAccounts()  {
        try {
            List<Account> accounts =accountDAO.findAll();

            if(accounts.isEmpty()){
                System.out.println("No accounts found");
                return;
            }

            printAccountsTable(accounts);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account findAccountOrThrow(String accountNumber) throws AccountNotFoundException, SQLException {

        Optional<Account> optional = accountDAO.findByAccountNumber(accountNumber);
        return optional.orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    private Account getLoggedInAccount() {
        if (!userSession.isLoggedIn()) {
            throw new IllegalStateException("No account is currently logged in.");
        }
        return userSession.getCurrentAccount();
    }

    public String generateAccountNumber() {
        long seed = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        return String.format("ACC-%010d", seed);
    }

    private void printAccountSummary(Account account) {
        System.out.println("==================================");
        System.out.printf("%-18s : %s%n", "Account Number", account.getAccountNumber());
        System.out.printf("%-18s : %s%n", "Account Name", account.getAccountName());
        System.out.printf("%-18s : %.2f%n", "Account Balance", account.getBalance());
        if (account.getCreatedAt() != null) {
            System.out.printf("%-18s : %s%n", "Created At", account.getCreatedAt().toString().replace("T", " "));
        }
        System.out.println("==================================");


    }

    private void printAccountsTable(List<Account> accounts) {
        String line = "+----+-----------------------+-------------------------+---------------+---------------------+";
        System.out.println(line);
        System.out.printf("| %-2s | %-21s | %-23s | %-13s | %-19s |%n",
                "No", "Account Number", "Account Name", "Balance (PHP)", "Created At");
        System.out.println(line);

        int i = 1;
        for (Account a : accounts) {
            System.out.printf("| %-2d | %-21s | %-23s | %13.2f | %-19s |%n",
                    i++,
                    a.getAccountNumber(),
                    a.getAccountName(),
                    a.getBalance(),
                    a.getCreatedAt() != null ? a.getCreatedAt().toString().replace("T", " ") : "—"
            );
        }
        System.out.println(line);
        System.out.println("  Total accounts: " + accounts.size());
    }


}
