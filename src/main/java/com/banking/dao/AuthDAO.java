package com.banking.dao;

import com.banking.model.AccountCredential;

import java.sql.SQLException;
import java.util.Optional;

public interface AuthDAO {

    Optional<AccountCredential> findByUsername(String username) throws SQLException;

    void save(AccountCredential credential) throws SQLException;

    boolean existsByAccountNumber(String accountNumber) throws SQLException;
}
