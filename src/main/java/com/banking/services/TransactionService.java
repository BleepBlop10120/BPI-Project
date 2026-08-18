package com.banking.services;

import com.banking.config.DBConnection;
import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidTransactionException;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import com.banking.session.UserSession;
import com.banking.util.ReferenceNumberGenerator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class TransactionService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final AccountService accountService;
    private final UserSession userSession;
    public final Scanner scanner;

    private static final int MINI_STATEMENT_LIMIT = 10;
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TransactionService(AccountDAO accountDAO, TransactionDAO transactionDAO,
                               AccountService accountService, Scanner scanner,
                               UserSession userSession) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        this.accountService = accountService;
        this.scanner = scanner;
        this.userSession = userSession;
    }

    public void deposit() {
        System.out.println("\n------ DEPOSIT ------");

        try {
            Account account = getLoggedInAccount();
            BigDecimal amount = promptPositiveAmount("Enter amount to deposit: ");

            BigDecimal newBalance = account.getBalance().add(amount);
            accountDAO.updateBalance(account.getAccountNumber(), newBalance);

            Transaction transaction = new Transaction(
                    account.getAccountNumber(),
                    TransactionType.DEPOSIT,
                    amount,
                    newBalance,
                    ReferenceNumberGenerator.generate(),
                    "Cash Deposit"
            );
            transactionDAO.save(transaction);

            System.out.println("Transaction deposited successfully");
            printTransactionReceipt(transaction, account.getAccountName(), account.getBalance());
            account.setBalance(newBalance);

        } catch (IllegalStateException | InvalidTransactionException e) {
            System.out.println("Error " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    public void withdraw() {
        System.out.println("\n------ WITHDRAW ------");

        try {
            Account account = getLoggedInAccount();
            BigDecimal amount = promptPositiveAmount("Enter amount to withdraw: ");

            validateSufficientBalance(account, amount);

            BigDecimal newBalance = account.getBalance().subtract(amount);
            accountDAO.updateBalance(account.getAccountNumber(), newBalance);


            Transaction transaction = new Transaction(
                    account.getAccountNumber(),
                    TransactionType.WITHDRAW,
                    amount,
                    newBalance,
                    ReferenceNumberGenerator.generate(),
                    "Cash Withdrawal"
            );
            transactionDAO.save(transaction);

            printTransactionReceipt(transaction, account.getAccountName(), account.getBalance());
            account.setBalance(newBalance);

        } catch (IllegalStateException | InvalidTransactionException |
                 InsufficientBalanceException e) {
            System.out.println("Error " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    public void transfer() {
        System.out.println("\n------ FUND TRANSFER ------");

        String receiverNumber;
        BigDecimal amount;
        Account sender;
        Account receiver;

        // The logged-in account is automatically the sender.
        try {
            sender = getLoggedInAccount();

            System.out.println("Sender account: " + sender.getAccountNumber());
            System.out.println("Sender name   : " + sender.getAccountName());

            System.out.print("Enter receiver account number : ");
            receiverNumber = scanner.nextLine().trim();

            if (sender.getAccountNumber().equalsIgnoreCase(receiverNumber)) {
                throw new InvalidTransactionException(
                        "Sender and receiver cannot be the same account."
                );
            }

            amount = promptPositiveAmount("Enter transfer amount         : ");

            receiver = accountService.findAccountOrThrow(receiverNumber);
            validateSufficientBalance(sender, amount);

        } catch (AccountNotFoundException | InvalidTransactionException |
                 InsufficientBalanceException | IllegalStateException e) {
            System.out.println("[ERROR] " + e.getMessage());
            return;
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
            return;
        }

        BigDecimal senderNewBalance = sender.getBalance().subtract(amount);
        BigDecimal receiverNewBalance = receiver.getBalance().add(amount);

        String sharedRef = ReferenceNumberGenerator.generate();

        Transaction transferOut = new Transaction(
                sender.getAccountNumber(),
                TransactionType.TRANSFER_OUT,
                amount,
                senderNewBalance,
                sharedRef,
                "Transfer to " + receiver.getAccountNumber() + " (" + receiver.getAccountName() + ")"
        );

        Transaction transferIn = new Transaction(
                receiver.getAccountNumber(),
                TransactionType.TRANSFER_IN,
                amount,
                receiverNewBalance,
                ReferenceNumberGenerator.generate(),
                "Transfer from " + sender.getAccountNumber() + " (" + sender.getAccountName() + ")"
        );

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                accountDAO.updateBalance(conn, sender.getAccountNumber(), senderNewBalance);
                accountDAO.updateBalance(conn, receiver.getAccountNumber(), receiverNewBalance);
                transactionDAO.save(conn, transferOut);
                transactionDAO.save(conn, transferIn);

                conn.commit();

                System.out.println("\n[SUCCESS] Fund transfer completed!");
                printTransactionReceipt(transferOut, sender.getAccountName(), sender.getBalance());
                sender.setBalance(senderNewBalance);
            } catch (SQLException e) {
                try {
                    conn.rollback();
                    System.out.println("Transfer rolled back for ref: " + sharedRef);
                } catch (SQLException rollbackEx) {
                    System.out.println("Rollback failed for ref: " + sharedRef);
                }
                System.out.println("[ERROR] Transfer failed and was rolled back: " + e.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }

        } catch (SQLException e) {
            System.out.println("[ERROR] Could not connect to the database: " + e.getMessage());
        }
    }

    public void viewTransactionHistory() {
        System.out.println("\n------ TRANSACTION HISTORY ------");

        try {
            Account account = getLoggedInAccount();
            String accountNumber = account.getAccountNumber();

            List<Transaction> transactions = transactionDAO.findByAccountNumber(accountNumber);

            if (transactions.isEmpty()) {
                System.out.println("No transactions found for account: " + accountNumber);
                return;
            }

            System.out.printf("%nTransaction History — %s%n", accountNumber);
            printTransactionsTable(transactions);

        } catch (IllegalStateException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    public void miniStatement() {
        System.out.println("\n------ MINI STATEMENT (Last " + MINI_STATEMENT_LIMIT + " Transactions) ------");

        try {
            Account account = getLoggedInAccount();
            String accountNumber = account.getAccountNumber();

            List<Transaction> transactions =
                    transactionDAO.findRecentTransactions(accountNumber, MINI_STATEMENT_LIMIT);

            System.out.println("\n  Account : " + account.getAccountNumber());
            System.out.println("  Name    : " + account.getAccountName());
            System.out.printf("  Balance : PHP %.2f%n%n", account.getBalance());

            if (transactions.isEmpty()) {
                System.out.println("  No transactions found.");
                return;
            }

            printTransactionsTable(transactions);

        } catch (IllegalStateException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private Account getLoggedInAccount() {
        if (!userSession.isLoggedIn()) {
            throw new IllegalStateException("No account is currently logged in.");
        }
        return userSession.getCurrentAccount();
    }

    private void validateSufficientBalance(Account account, BigDecimal amount)
            throws InsufficientBalanceException {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }
    }

    private BigDecimal promptPositiveAmount(String prompt) throws InvalidTransactionException {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            BigDecimal amount = new BigDecimal(input);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidTransactionException("Amount must be greater than zero.");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new InvalidTransactionException("Invalid amount: '" + input + "'");
        }
    }

    private void printTransactionReceipt(Transaction transaction, String accountName,
                                         BigDecimal currentBalance) {
        System.out.println("==================================");
        System.out.println("  Account         : " + transaction.getAccountNumber());
        System.out.println("  Account Name    : " + accountName);
        System.out.println("  Type            : " + transaction.getTransactionType().displayName());
        System.out.printf("  Current Balance : PHP %.2f%n", currentBalance);
        System.out.printf("  Amount          : PHP %.2f%n", transaction.getAmount());
        System.out.printf("  Balance After   : PHP %.2f%n", transaction.getBalanceAfter());
        System.out.println("  Reference       : " + transaction.getReferenceNumber());
        if (transaction.getRemarks() != null && !transaction.getRemarks().isEmpty()) {
            System.out.println("  Remarks         : " + transaction.getRemarks());
        }
        System.out.println("==================================");
    }

    private void printTransactionsTable(List<Transaction> transactions) {
        String line = "+----+---------------------+--------------+--------------+--------------+---------------------+";
        System.out.println(line);
        System.out.printf("| %-2s | %-19s | %-12s | %-12s | %-12s | %-19s |%n",
                "No", "Reference", "Type", "Amount", "Balance Aftr", "Date/Time");
        System.out.println(line);

        int i = 1;
        for (Transaction t : transactions) {
            System.out.printf("| %-2d | %-19s | %-12s | %12.2f | %12.2f | %-19s |%n",
                    i++,
                    t.getReferenceNumber().length() > 19
                            ? t.getReferenceNumber().substring(0, 19) : t.getReferenceNumber(),
                    t.getTransactionType().displayName(),
                    t.getAmount(),
                    t.getBalanceAfter(),
                    t.getCreatedAt() != null ? t.getCreatedAt().format(DISPLAY_FMT) : "—"
            );
        }
        System.out.println(line);
    }
}
