package org.libraryexpress.infrastructure.api.guard;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.domain.core.exception.InvalidCredentialsException;
import org.libraryexpress.domain.user.enums.Role;
import org.libraryexpress.domain.user.repository.UserRepository;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.gateway.aws.SecretProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSuperAdminGuard")
class CreateSuperAdminGuardTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecretProvider secretProvider;

    @Test
    @DisplayName("Should reject a missing setup token before repository or provider access")
    void shouldRejectMissingToken_whenHeaderIsMissing() {
        HttpContextRequest request = mock(HttpContextRequest.class);
        CreateSuperAdminGuard guard = new CreateSuperAdminGuard(userRepository, secretProvider, "setup-secret");

        assertThrows(InvalidCredentialsException.class, () -> guard.check(request));

        verify(userRepository, never()).existsByRole(Role.SA);
        verify(secretProvider, never()).get("setup-secret");
    }

    @Test
    @DisplayName("Should reject a blank setup token")
    void shouldRejectBlankToken_whenHeaderIsBlank() {
        HttpContextRequest request = mock(HttpContextRequest.class);
        when(request.getHeader("X-Setup-Token")).thenReturn("  ");
        CreateSuperAdminGuard guard = new CreateSuperAdminGuard(userRepository, secretProvider, "setup-secret");

        assertThrows(InvalidCredentialsException.class, () -> guard.check(request));
    }

    @Test
    @DisplayName("Should reject when a super administrator already exists")
    void shouldReject_whenSuperAdminAlreadyExists() {
        HttpContextRequest request = mock(HttpContextRequest.class);
        when(request.getHeader("X-Setup-Token")).thenReturn("fake-token");
        when(userRepository.existsByRole(Role.SA)).thenReturn(true);
        CreateSuperAdminGuard guard = new CreateSuperAdminGuard(userRepository, secretProvider, "setup-secret");

        assertThrows(InvalidCredentialsException.class, () -> guard.check(request));

        verify(secretProvider, never()).get("setup-secret");
    }

    @Test
    @DisplayName("Should reject when the setup secret is not configured")
    void shouldReject_whenSecretDoesNotExist() {
        HttpContextRequest request = mock(HttpContextRequest.class);
        when(request.getHeader("X-Setup-Token")).thenReturn("fake-token");
        when(userRepository.existsByRole(Role.SA)).thenReturn(false);
        when(secretProvider.get("setup-secret")).thenReturn(Optional.empty());
        CreateSuperAdminGuard guard = new CreateSuperAdminGuard(userRepository, secretProvider, "setup-secret");

        assertThrows(InvalidCredentialsException.class, () -> guard.check(request));
    }

    @Test
    @DisplayName("Should reject a wrong setup token")
    void shouldRejectWrongToken_whenTokenDoesNotMatch() {
        HttpContextRequest request = mock(HttpContextRequest.class);
        when(request.getHeader("X-Setup-Token")).thenReturn("short");
        when(userRepository.existsByRole(Role.SA)).thenReturn(false);
        when(secretProvider.get("setup-secret")).thenReturn(Optional.of("much-longer-secret"));
        CreateSuperAdminGuard guard = new CreateSuperAdminGuard(userRepository, secretProvider, "setup-secret");

        assertThrows(InvalidCredentialsException.class, () -> guard.check(request));
    }

    @Test
    @DisplayName("Should accept a matching token with surrounding whitespace")
    void shouldAcceptMatchingToken_whenTokenIsSurroundedByWhitespace() {
        HttpContextRequest request = mock(HttpContextRequest.class);
        when(request.getHeader("X-Setup-Token")).thenReturn("\n  fake-token  \n");
        when(userRepository.existsByRole(Role.SA)).thenReturn(false);
        when(secretProvider.get("setup-secret")).thenReturn(Optional.of("  fake-token\n"));
        CreateSuperAdminGuard guard = new CreateSuperAdminGuard(userRepository, secretProvider, "setup-secret");

        assertDoesNotThrow(() -> guard.check(request));
    }

    @Test
    @DisplayName("Should propagate provider infrastructure failures unchanged")
    void shouldPropagateInfrastructureFailure_whenProviderFails() {
        HttpContextRequest request = mock(HttpContextRequest.class);
        when(request.getHeader("X-Setup-Token")).thenReturn("fake-token");
        when(userRepository.existsByRole(Role.SA)).thenReturn(false);
        RuntimeException failure = new RuntimeException("provider unavailable");
        when(secretProvider.get("setup-secret")).thenThrow(failure);
        CreateSuperAdminGuard guard = new CreateSuperAdminGuard(userRepository, secretProvider, "setup-secret");

        RuntimeException thrown =
                (RuntimeException) Assertions.assertThrows(RuntimeException.class, () -> guard.check(request));

        Assertions.assertSame(failure, thrown);
    }
}
