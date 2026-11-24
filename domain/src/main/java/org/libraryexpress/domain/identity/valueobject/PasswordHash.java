package org.libraryexpress.domain.identity.valueobject;

import org.libraryexpress.domain.core.exception.InvalidArgumentException;

/** An already computed Argon2id hash in PHC string format. */
public record PasswordHash(String value) {

    private static final String PREFIX = "$argon2id$";
    private static final int MAX_LENGTH = 255; // identity.password_hash is VARCHAR(255)

    public PasswordHash {
        if (value == null || !value.startsWith(PREFIX))
            throw new InvalidArgumentException("Password hash must be an Argon2id PHC string");

        if (value.length() > MAX_LENGTH) throw new InvalidArgumentException("Password hash exceeds maximum length");
    }

    @Override
    public String toString() {
        return "PasswordHash[****]";
    }
}
