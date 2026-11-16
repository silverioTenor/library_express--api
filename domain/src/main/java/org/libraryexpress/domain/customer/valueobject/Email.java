package org.libraryexpress.domain.customer.valueobject;

import org.libraryexpress.domain.customer.exception.InvalidEmailException;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+(?<![!#$%&'*+/=?^_`{|}~.-])@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    public Email {
        if (value == null || value.isBlank())  throw new InvalidEmailException("Email cannot be null or empty");

        if (value.length() > 254) throw new InvalidEmailException("Email exceeds maximum allowed length");

        if (!EMAIL_PATTERN.matcher(value).matches()) throw new InvalidEmailException("Invalid email format");
    }
}
