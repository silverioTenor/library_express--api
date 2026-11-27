package org.libraryexpress.application.user.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.support.PassthroughTransactionManager;
import org.libraryexpress.application.user.dto.request.CreateSuperAdminDTO;
import org.libraryexpress.application.user.dto.response.UserCreatedDTO;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.identity.security.PasswordHasher;
import org.libraryexpress.domain.identity.valueobject.PasswordHash;
import org.libraryexpress.domain.user.entity.User;
import org.libraryexpress.domain.user.enums.Role;
import org.libraryexpress.domain.user.repository.UserRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSuperAdmin use case")
class CreateSuperAdminTest {

    private static final String VALID_HASH = "$argon2id$v=19$m=65536,t=3,p=1$c2FsdA$aGFzaA";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Test
    @DisplayName("Should create and persist an active super admin when the request is valid")
    void shouldCreateSuperAdmin_whenRequestIsValid() {
        CreateSuperAdminDTO request =
                new CreateSuperAdminDTO("Library Admin", "admin@example.com", "N0tCommon-LongPassword!2026");
        PasswordHash passwordHash = PasswordHash.of(VALID_HASH);
        when(passwordHasher.hash(any())).thenReturn(passwordHash);
        CreateSuperAdmin useCase =
                new CreateSuperAdmin(userRepository, passwordHasher, new PassthroughTransactionManager());

        UserCreatedDTO response = useCase.execute(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).create(userCaptor.capture());
        User createdUser = userCaptor.getValue();
        assertEquals(createdUser.getId().toString(), response.id());
        assertEquals("Library Admin", createdUser.getName());
        assertEquals(Role.SA, createdUser.getRole());
        assertEquals("admin@example.com", createdUser.getIdentity().getEmail().value());
        assertEquals(
                VALID_HASH,
                createdUser.getIdentity().getPasswordHash().orElseThrow().value());
    }

    @Test
    @DisplayName("Should reject a super admin and abort persistence when the password is invalid")
    void shouldRejectSuperAdmin_whenPasswordIsTooShort() {
        CreateSuperAdminDTO request = new CreateSuperAdminDTO("Library Admin", "admin@example.com", "short");
        CreateSuperAdmin useCase =
                new CreateSuperAdmin(userRepository, passwordHasher, new PassthroughTransactionManager());

        assertThrows(InvalidArgumentException.class, () -> useCase.execute(request));

        verify(passwordHasher, never()).hash(any());
        verify(userRepository, never()).create(any(User.class));
    }

    @Test
    @DisplayName("Should hash the password outside the transaction before persistence")
    void shouldHashOutsideTransaction_whenRequestIsValid() {
        CreateSuperAdminDTO request =
                new CreateSuperAdminDTO("Library Admin", "admin@example.com", "N0tCommon-LongPassword!2026");
        PasswordHash passwordHash = PasswordHash.of(VALID_HASH);
        when(passwordHasher.hash(any())).thenReturn(passwordHash);
        CreateSuperAdmin useCase =
                new CreateSuperAdmin(userRepository, passwordHasher, new PassthroughTransactionManager());

        useCase.execute(request);

        InOrder inOrder = inOrder(passwordHasher, userRepository);
        inOrder.verify(passwordHasher).hash(any());
        inOrder.verify(userRepository).create(any(User.class));
    }
}
