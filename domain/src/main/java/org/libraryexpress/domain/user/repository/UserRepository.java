package org.libraryexpress.domain.user.repository;

import java.util.Optional;
import java.util.UUID;
import org.libraryexpress.domain.user.entity.User;
import org.libraryexpress.domain.user.enums.Role;

public interface UserRepository {

    /**
     * Persists the user together with its identity. Atomicity is the caller's transaction. Requires an active
     * transaction.
     */
    void create(User user);

    Optional<User> getById(UUID id);

    Optional<User> getByEmail(String email);

    boolean existsByRole(Role role);
}
