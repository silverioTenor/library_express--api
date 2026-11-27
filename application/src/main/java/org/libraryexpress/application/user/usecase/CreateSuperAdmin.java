package org.libraryexpress.application.user.usecase;

import org.libraryexpress.application.user.dto.request.CreateSuperAdminDTO;
import org.libraryexpress.application.user.dto.response.UserCreatedDTO;
import org.libraryexpress.domain.core.repository.TransactionManager;
import org.libraryexpress.domain.identity.entity.Identity;
import org.libraryexpress.domain.identity.security.PasswordHasher;
import org.libraryexpress.domain.identity.valueobject.PasswordHash;
import org.libraryexpress.domain.user.entity.User;
import org.libraryexpress.domain.user.enums.Role;
import org.libraryexpress.domain.user.repository.UserRepository;

public class CreateSuperAdmin {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TransactionManager transaction;

    public CreateSuperAdmin(
            UserRepository userRepository, PasswordHasher passwordHasher, TransactionManager transaction) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.transaction = transaction;
    }

    public UserCreatedDTO execute(CreateSuperAdminDTO createDto) {
        PasswordHash passwordHash = Identity.createPasswordHash(passwordHasher, createDto.password());
        Identity identity = Identity.createActive(createDto.email(), passwordHash);
        User user = User.create(createDto.name(), Role.SA, identity, null);

        transaction.execute(() -> userRepository.create(user));
        return new UserCreatedDTO(user.getId().toString());
    }
}
