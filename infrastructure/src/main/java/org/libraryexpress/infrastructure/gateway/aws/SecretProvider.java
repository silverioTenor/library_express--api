package org.libraryexpress.infrastructure.gateway.aws;

import java.util.Optional;

public interface SecretProvider {
    Optional<String> get(String id);
}
