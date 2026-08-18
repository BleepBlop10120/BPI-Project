package com.banking.dao.impl;

import com.banking.config.DBConnection;
import com.banking.dao.AccountDAO;
import com.banking.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class AccountDAOImpl implements AccountDAO {

    private static final String SQL_INSERT =
            "INSERT INTO accounts (account_number, account_name, balance) VALUES (?, ?, ?)";

    private static final String SQL_FIND_BY_ACCOUNT_NUMBER =
            "SELECT account_id, account_number, account_name, balance, created_at, updated_at FROM accounts WHERE account_number = ?";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM accounts";

    private static final String SQL_UPDATE_BALANCE =
            "UPDATE accounts SET balance = ? WHERE account_number = ?";


    @Override
    public void createAccount(Account account) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, account.getAccountNumber());
            preparedStatement.setString(2, account.getAccountName());
            preparedStatement.setBigDecimal(3, account.getBalance());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setAccountID(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_FIND_BY_ACCOUNT_NUMBER)) {

            preparedStatement.setString(1, accountNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {

                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                throw e;
            }

        }

    }

    @Override
    public List<Account> findAll() throws SQLException {

        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(SQL_FIND_ALL);
        ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                accounts.add(mapRow(resultSet));
            }
            return accounts;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateBalance(String accountNumber, BigDecimal balance) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            updateBalance(conn, accountNumber, balance);
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateBalance(Connection conn, String accountNumber, BigDecimal balance) throws  SQLException {
        try(PreparedStatement preparedStatement = conn.prepareStatement(SQL_UPDATE_BALANCE)) {
            preparedStatement.setBigDecimal(1, balance);
            preparedStatement.setString(2, accountNumber);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No rows affected!");
            }

        }
    }


    private Account mapRow(ResultSet rs) throws SQLException {

        Account account = new Account();
        account.setAccountID(rs.getLong("account_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setAccountName(rs.getString("account_name"));
        account.setBalance(rs.getBigDecimal("balance"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            account.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            account.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return account;
    }
}
