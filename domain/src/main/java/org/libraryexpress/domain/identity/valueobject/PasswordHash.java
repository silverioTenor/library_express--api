package org.libraryexpress.domain.identity.valueobject;

import org.libraryexpress.domain.core.exception.InvalidArgumentException;

/** An already computed Argon2id hash in PHC string format. */
public final class PasswordHash {

    private static final String PREFIX = "$argon2id$";
    private static final int MAX_LENGTH = 255; // identity.password_hash is VARCHAR(255)

    private final String value;

    private PasswordHash(String value) {
        this.value = value;
    }

    public static PasswordHash of(String password) {
        if (password == null || !password.startsWith(PREFIX))
            throw new InvalidArgumentException("Password hash must be an Argon2id PHC string");

        if (password.length() > MAX_LENGTH) throw new InvalidArgumentException("Password hash exceeds maximum length");

        return new PasswordHash(password);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "PasswordHash[****]";
    }
}
