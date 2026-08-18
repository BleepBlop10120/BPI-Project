package com.banking.session;

import com.banking.model.Account;

/**
 * Holds the account that is currently logged in.
 *
 * This object is shared by the menu and services through constructor injection.
 */
public class UserSession {

    private Account currentAccount;

    public void login(Account account) {
        this.currentAccount = account;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public boolean isLoggedIn() {
        return currentAccount != null;
    }

    public void logout() {
        currentAccount = null;
    }
}
