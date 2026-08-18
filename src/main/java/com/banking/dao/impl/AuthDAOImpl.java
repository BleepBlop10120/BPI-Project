package com.banking.dao.impl;

import com.banking.config.DBConnection;
import com.banking.dao.AuthDAO;
import com.banking.model.AccountCredential;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class AuthDAOImpl implements AuthDAO {

    private static final String SQL_INSERT =
            "INSERT INTO account_credentials (account_number, username, password_hash, salt) VALUES (?, ?, ?, ?)";

    private static final String SQL_FIND_BY_USERNAME =
            "SELECT credential_id, account_number, username, password_hash, salt " +
            "FROM account_credentials WHERE username = ?";

    private static final String SQL_EXISTS_BY_ACCOUNT_NUMBER =
            "SELECT 1 FROM account_credentials WHERE account_number = ?";

    @Override
    public Optional<AccountCredential> findByUsername(String username) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_FIND_BY_USERNAME)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void save(AccountCredential credential) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, credential.getAccountNumber());
            preparedStatement.setString(2, credential.getUsername());
            preparedStatement.setString(3, credential.getPasswordHash());
            preparedStatement.setString(4, credential.getSalt());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    credential.setCredentialId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_EXISTS_BY_ACCOUNT_NUMBER)) {

            preparedStatement.setString(1, accountNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

    private AccountCredential mapRow(ResultSet rs) throws SQLException {
        AccountCredential credential = new AccountCredential();
        credential.setCredentialId(rs.getLong("credential_id"));
        credential.setAccountNumber(rs.getString("account_number"));
        credential.setUsername(rs.getString("username"));
        credential.setPasswordHash(rs.getString("password_hash"));
        credential.setSalt(rs.getString("salt"));
        return credential;
    }
}
