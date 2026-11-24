package org.libraryexpress.domain.identity.security;

import org.libraryexpress.domain.identity.valueobject.PasswordHash;
import org.libraryexpress.domain.identity.valueobject.PlainPassword;

public interface PasswordHasher {
    PasswordHash hash(PlainPassword password);

    boolean verify(PlainPassword password, PasswordHash passwordHash);
    /** True when the stored hash was made with parameters weaker than the current ones (or is unreadable). */
    boolean needsRehash(PasswordHash passwordHash);
}
