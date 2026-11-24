package org.libraryexpress.domain.core.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

public final class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+(?<![!#$%&'*+/=?^_`{|}~.-])@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    private final String value;

    private Email(String value) {
        validate(value);
        this.value = value;
    }

    public static Email of(String email) {
        return new Email(email);
    }

    private static void validate(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidArgumentException("Email cannot be null or empty");
        }
        if (email.length() > 254) {
            throw new InvalidArgumentException("Email exceeds maximum allowed length");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidArgumentException("Invalid email format");
        }
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Email email)) return false;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
