package org.libraryexpress.domain.identity.valueobject;

import java.text.Normalizer;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.identity.policy.PasswordPolicy;

/** A password as typed by the user. Never persisted and never logged. */
public record PlainPassword(String value) {

    public PlainPassword {
        if (value == null) throw new InvalidArgumentException("Password cannot be null");

        value = Normalizer.normalize(value, Normalizer.Form.NFKC);
        int length = value.codePointCount(0, value.length());

        if (length < PasswordPolicy.MIN_LENGTH)
            throw new InvalidArgumentException(
                    "Password must have at least " + PasswordPolicy.MIN_LENGTH + " characters");

        if (length > PasswordPolicy.MAX_LENGTH)
            throw new InvalidArgumentException(
                    "Password must have at most " + PasswordPolicy.MAX_LENGTH + " characters");

        if (PasswordPolicy.isDenied(value)) throw new InvalidArgumentException("Password is too common");
    }

    @Override
    public String toString() {
        return "PlainPassword[****]";
    }
}
