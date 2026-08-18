package com.banking.dao.impl;

import com.banking.config.DBConnection;
import com.banking.dao.TransactionDAO;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TransactionDAOImpl implements TransactionDAO {

    private static final String SQL_INSERT =
            "INSERT INTO transactions " +
                    "(account_number, transaction_type, amount, balance_after, reference_number, remarks) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";


    private static final String SQL_FIND_BY_ACCOUNT =
            "SELECT transaction_id, account_number, transaction_type, amount, balance_after, " +
                    "       reference_number, remarks, created_at " +
                    "FROM transactions " +
                    "WHERE account_number = ? " +
                    "ORDER BY created_at DESC";

    private static final String SQL_FIND_RECENT =
            "SELECT transaction_id, account_number, transaction_type, amount, balance_after, " +
                    "       reference_number, remarks, created_at " +
                    "FROM transactions " +
                    "WHERE account_number = ? " +
                    "ORDER BY created_at DESC " +
                    "LIMIT ?";



    public void save(Transaction transaction) throws SQLException {
        try (Connection conn = DBConnection.getConnection()){
            save(conn, transaction);
        } catch(SQLException e){
            System.out.println("Error while saving transaction: " + e.getMessage());
            throw e;
        }
    }


    public void save(Connection conn, Transaction transaction) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, transaction.getAccountNumber());
            ps.setString(2, transaction.getTransactionType().name());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setBigDecimal(4,transaction.getBalanceAfter());
            ps.setString(5,transaction.getReferenceNumber());
            ps.setString(6,transaction.getRemarks());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    transaction.setTransactionId(rs.getLong(1));
                }
            }

        }

}

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_FIND_BY_ACCOUNT)) {
            preparedStatement.setString(1, accountNumber);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapRow(rs));
                    }
            }
            return transactions;
        } catch(SQLException e){
            System.out.println("Error while finding transactions by account number: " + e.getMessage());
            throw e;
        }

    }

    @Override
    public List<Transaction> findRecentTransactions(String accountNumber, int limit) throws SQLException {
        List<Transaction> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_RECENT)) {

            ps.setString(1, accountNumber);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw e;
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(rs.getLong("transaction_id"));
        transaction.setAccountNumber(rs.getString("account_number"));
        transaction.setTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));
        transaction.setReferenceNumber(rs.getString("reference_number"));
        transaction.setRemarks(rs.getString("remarks"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toLocalDateTime());
        }

        return transaction;
    }


}
