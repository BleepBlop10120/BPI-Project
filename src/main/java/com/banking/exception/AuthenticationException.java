package com.banking.exception;

/**
 * Thrown when a login attempt fails (unknown username or wrong password).
 */
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }
}
