package org.libraryexpress.domain.identity.valueobject;

import java.text.Normalizer;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.identity.policy.PasswordPolicy;

/** A password as typed by the user. Never persisted and never logged. */
public final class PlainPassword {

    private final String value;

    private PlainPassword(String value) {
        this.value = value;
    }

    public static PlainPassword of(String password) {
        if (password == null) throw new InvalidArgumentException("Password cannot be null");

        password = Normalizer.normalize(password, Normalizer.Form.NFKC);
        int length = password.codePointCount(0, password.length());

        if (length < PasswordPolicy.MIN_LENGTH)
            throw new InvalidArgumentException(
                    "Password must have at least " + PasswordPolicy.MIN_LENGTH + " characters");

        if (length > PasswordPolicy.MAX_LENGTH)
            throw new InvalidArgumentException(
                    "Password must have at most " + PasswordPolicy.MAX_LENGTH + " characters");

        if (PasswordPolicy.isDenied(password)) throw new InvalidArgumentException("Password is too common");

        return new PlainPassword(password);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "PlainPassword[****]";
    }
}
