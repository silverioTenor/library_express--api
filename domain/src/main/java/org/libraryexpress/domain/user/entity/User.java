package org.libraryexpress.domain.user.entity;

import java.util.*;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.identity.entity.Identity;
import org.libraryexpress.domain.user.enums.Role;

/** An internal operator: profile, role and hierarchy. Owns its Identity. */
public class User {

    private static final int NAME_MAX_LENGTH = 150; // admin_user.name is VARCHAR(150)

    private final UUID id;
    private final String name;
    private final Role role;
    private final Identity identity;
    private final UUID createdBy;

    private User(UUID id, String name, Role role, Identity identity, UUID createdBy) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.identity = identity;
        this.createdBy = createdBy;

        this.validate();
    }

    /** The superAdmin has no creator; every other role must be created by someone. */
    public static User create(String name, Role role, Identity identity, UUID createdBy) {
        boolean hasCreator = createdBy != null;

        if (role == Role.SA && hasCreator) throw new InvalidArgumentException("The super admin cannot have a creator");
        if (role != Role.SA && !hasCreator)
            throw new InvalidArgumentException("A user other than the super admin must have a creator");

        return new User(UUID.randomUUID(), name, role, identity, hasCreator ? createdBy : null);
    }

    private void validate() {
        List<String> errors = new ArrayList<>();

        if (id == null) errors.add("User id cannot be null or empty");
        if (name == null || name.isBlank()) {
            errors.add("User name cannot be null or empty");
        } else if (name.length() > NAME_MAX_LENGTH) {
            errors.add("User name exceeds " + NAME_MAX_LENGTH + " characters");
        }
        if (role == null) errors.add("User role cannot be null");
        if (identity == null) errors.add("User identity cannot be null");

        if (!errors.isEmpty()) {
            throw new InvalidArgumentException(errors.toString());
        }
    }

    /** Rebuilds a persisted user. It trusts the stored creator (the FK is ON DELETE SET NULL). */
    public static User restore(UUID id, String name, Role role, Identity identity, UUID createdBy) {
        return new User(id, name, role, identity, createdBy);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public Identity getIdentity() {
        return identity;
    }

    public Optional<UUID> getCreatedBy() {
        return Optional.ofNullable(createdBy);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name=" + name + ", role=" + role + ", identity=" + identity + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
