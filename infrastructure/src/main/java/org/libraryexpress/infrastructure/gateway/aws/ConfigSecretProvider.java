package org.libraryexpress.infrastructure.gateway.aws;

import java.util.Optional;
import java.util.function.Function;

public final class ConfigSecretProvider implements SecretProvider {

    private final Function<String, String> lookup; // wraps the existing config reader

    public ConfigSecretProvider(Function<String, String> lookup) {
        this.lookup = lookup;
    }

    @Override
    public Optional<String> get(String id) {
        return Optional.ofNullable(lookup.apply(id)).filter(value -> !value.isBlank());
    }
}
