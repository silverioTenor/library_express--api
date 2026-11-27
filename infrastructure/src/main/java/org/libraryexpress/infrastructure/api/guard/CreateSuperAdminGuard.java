package org.libraryexpress.infrastructure.api.guard;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.libraryexpress.domain.core.exception.InvalidCredentialsException;
import org.libraryexpress.domain.user.enums.Role;
import org.libraryexpress.domain.user.repository.UserRepository;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.gateway.aws.SecretProvider;

public class CreateSuperAdminGuard implements Guard {

    static final String TOKEN_HEADER = "X-Setup-Token";

    private final UserRepository userRepository;
    private final SecretProvider secretProvider;
    private final String secretId;

    public CreateSuperAdminGuard(UserRepository userRepository, SecretProvider secretProvider, String secretId) {
        this.userRepository = userRepository;
        this.secretProvider = secretProvider;
        this.secretId = secretId;
    }

    @Override
    public void check(HttpContextRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException();
        }
        if (userRepository.existsByRole(Role.SA)) {
            throw new InvalidCredentialsException();
        }

        String storedToken = secretProvider.get(secretId).orElseThrow(InvalidCredentialsException::new);

        if (!matches(token.strip(), storedToken.strip())) {
            throw new InvalidCredentialsException();
        }
    }

    private static boolean matches(String token, String storedToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return MessageDigest.isEqual(
                    digest.digest(token.getBytes(StandardCharsets.UTF_8)),
                    digest.digest(storedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // SHA-256 is mandatory in every JRE
        }
    }
}
