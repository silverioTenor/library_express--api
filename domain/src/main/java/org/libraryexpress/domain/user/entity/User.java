package org.libraryexpress.domain.user.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.core.util.RandomGenerator;
import org.libraryexpress.domain.identity.entity.Identity;
import org.libraryexpress.domain.user.enums.Role;

/** An internal operator: profile, role and hierarchy. Owns its Identity. */
public class User {

    private static final int NAME_MAX_LENGTH = 150; // admin_user.name is VARCHAR(150)

    private final String id;
    private final String name;
    private final Role role;
    private final Identity identity;
    private final String createdBy;

    private User(String id, String name, Role role, Identity identity, String createdBy) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.identity = identity;
        this.createdBy = createdBy;

        this.validate();
    }

    /** The superAdmin has no creator; every other role must be created by someone. */
    public static User create(String name, Role role, Identity identity, String createdBy) {
        boolean hasCreator = createdBy != null && !createdBy.isBlank();

        if (role == Role.SUPER_ADMIN && hasCreator)
            throw new InvalidArgumentException("The super admin cannot have a creator");
        if (role != Role.SUPER_ADMIN && !hasCreator)
            throw new InvalidArgumentException("A user other than the super admin must have a creator");

        return new User(RandomGenerator.UUID(), name, role, identity, hasCreator ? createdBy : null);
    }

    private void validate() {
        List<String> errors = new ArrayList<>();

        if (id == null || id.isBlank()) errors.add("User id cannot be null or empty");
        if (name == null || name.isBlank()) errors.add("User name cannot be null or empty");
        if (name.length() > NAME_MAX_LENGTH) errors.add("User name exceeds " + NAME_MAX_LENGTH + " characters");
        if (role == null) errors.add("User role cannot be null");
        if (identity == null) errors.add("User identity cannot be null");

        if (!errors.isEmpty()) {
            throw new InvalidArgumentException(errors.toString());
        }
    }

    /** Rebuilds a persisted user. It trusts the stored creator (the FK is ON DELETE SET NULL). */
    public static User restore(String id, String name, Role role, Identity identity, String createdBy) {
        return new User(id, name, role, identity, createdBy);
    }

    public String getId() {
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

    public Optional<String> getCreatedBy() {
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