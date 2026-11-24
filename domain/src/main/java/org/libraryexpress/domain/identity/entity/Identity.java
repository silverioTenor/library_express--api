package org.libraryexpress.domain.identity.entity;

import java.time.Instant;
import java.util.*;

import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.core.valueobject.Email;
import org.libraryexpress.domain.identity.enums.IdentityStatus;
import org.libraryexpress.domain.identity.valueobject.PasswordHash;

/** Credentials and access state. Profile and hierarchy data live in User. */
public class Identity {

    private final UUID id;
    private final Email email;
    private final PasswordHash passwordHash;
    private final IdentityStatus status;
    private final int failedAttempts;
    private final Instant lockedUntil;

    private Identity(
            UUID id,
            Email email,
            PasswordHash passwordHash,
            IdentityStatus status,
            int failedAttempts,
            Instant lockedUntil) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;

        this.validate();
    }

    /** New identity waiting for its owner to define the password. */
    public static Identity createPending(String email) {
        return new Identity(
                UUID.randomUUID(), new Email(email), null, IdentityStatus.PENDING_ACTIVATION, 0, null);
    }

    /** New identity that is born active (the superAdmin bootstrap). */
    public static Identity createActive(String email, PasswordHash passwordHash) {
        return new Identity(UUID.randomUUID(), new Email(email), passwordHash, IdentityStatus.ACTIVE, 0, null);
    }

    private void validate() {
        List<String> errors = new ArrayList<>();

        if (id == null) errors.add("Identity id cannot be null");
        if (email == null) errors.add("Identity email cannot be null");
        if (status == null) errors.add("Identity status cannot be null");
        if (failedAttempts < 0) errors.add("Failed attempts cannot be negative");

        boolean pending = status == IdentityStatus.PENDING_ACTIVATION;
        if (pending && passwordHash != null)
            errors.add("A pending identity cannot have a password hash");
        if (!pending && passwordHash == null)
            errors.add("An identity that is not pending must have a password hash");

        if (!errors.isEmpty()) {
            throw new InvalidArgumentException(errors.toString());
        }
    }

    public static Identity restore(
            UUID id,
            String email,
            String passwordHash,
            IdentityStatus status,
            int failedAttempts,
            Instant lockedUntil) {
        PasswordHash hash = passwordHash == null ? null : new PasswordHash(passwordHash);
        return new Identity(id, new Email(email), hash, status, failedAttempts, lockedUntil);
    }

    public UUID getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public Optional<PasswordHash> getPasswordHash() {
        return Optional.ofNullable(passwordHash);
    }

    public IdentityStatus getStatus() {
        return status;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public Optional<Instant> getLockedUntil() {
        return Optional.ofNullable(lockedUntil);
    }

    @Override
    public String toString() {
        return "Identity{id=" + id + ", email=" + email + ", status=" + status + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Identity identity)) return false;
        return Objects.equals(id, identity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}