package org.libraryexpress.infrastructure.gateway.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.UnitTest;

@UnitTest
@DisplayName("ConfigSecretProvider")
class ConfigSecretProviderTest {

    @Test
    @DisplayName("Should return the configured value when the key is present")
    void shouldReturnValue_whenKeyIsPresent() {
        ConfigSecretProvider provider = new ConfigSecretProvider(key -> "fake-secret");

        Optional<String> result = provider.get("SETUP_SECRET");

        assertEquals("fake-secret", result.orElseThrow());
    }

    @Test
    @DisplayName("Should return empty when the key is absent or blank")
    void shouldReturnEmpty_whenKeyIsAbsentOrBlank() {
        ConfigSecretProvider absentProvider = new ConfigSecretProvider(key -> null);
        ConfigSecretProvider blankProvider = new ConfigSecretProvider(key -> " ");

        assertTrue(absentProvider.get("SETUP_SECRET").isEmpty());
        assertTrue(blankProvider.get("SETUP_SECRET").isEmpty());
        assertTrue(blankProvider.get(" ").isEmpty());
    }
}
