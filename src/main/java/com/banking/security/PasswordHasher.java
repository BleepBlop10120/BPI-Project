package com.banking.security;

/**
 * Abstraction over password hashing so {@code AuthService} never touches
 * plaintext comparisons or a specific hashing algorithm directly.
 * Injected via constructor, same as the DAOs.
 */
public interface PasswordHasher {

    /** Generates a new random salt, Base64-encoded. */
    String generateSalt();

    /** Hashes {@code password} using the given Base64-encoded salt. */
    String hash(String password, String saltBase64);

    /** Returns true if {@code password}, hashed with {@code saltBase64}, matches {@code expectedHash}. */
    boolean verify(String password, String saltBase64, String expectedHash);
}
