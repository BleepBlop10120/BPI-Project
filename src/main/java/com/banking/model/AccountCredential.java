package com.banking.model;

public class AccountCredential {

    private Long credentialId;
    private String accountNumber;
    private String username;
    private String passwordHash;
    private String salt;

    public AccountCredential(String accountNumber, String username, String passwordHash, String salt) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public AccountCredential() {
    }

    public Long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
