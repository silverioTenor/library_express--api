package org.libraryexpress.domain.user.repository;

import java.util.Optional;
import org.libraryexpress.domain.user.entity.User;

public interface UserRepository {

    /** Persists the user together with its identity. Atomicity is the caller's transaction. */
    void create(User user);

    Optional<User> getById(String id);

    Optional<User> getByEmail(String email);
}