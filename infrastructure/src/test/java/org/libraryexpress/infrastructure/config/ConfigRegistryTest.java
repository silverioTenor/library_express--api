package org.libraryexpress.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.UnitTest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("ConfigRegistry - Security & Fail-Fast Unit Test")
class ConfigRegistryTest {

    @Test
    @DisplayName("Should trigger a fatal IllegalStateException boundary when evaluating a missing mandatory environment token key")
    void shouldFailFast_whenMandatoryKeyIsCompletelyAbsent() throws Exception {
        // Using reflection to invoke the private cascade core helper logic cleanly
        Method resolveMethod = ConfigRegistry.class.getDeclaredMethod("resolve", String.class, String.class);
        resolveMethod.setAccessible(true);

        // Act & Assert: Simulating an environment call for an unregistered key with zero property failbacks and zero defaults
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            try {
                resolveMethod.invoke(null, "COMPLETELY_ABSENT_MANDATORY_KEY_802", null);
            } catch (Exception e) {
                throw e.getCause(); // Extracting the actual underlying target exception
            }
        });

        assertTrue(exception.getMessage().contains("is mandatory but was not defined"));
    }

    @Test
    @DisplayName("Should prioritize the APP_ENV system property over dotenv configuration")
    void shouldPrioritizeSystemProperty_whenResolvingAppEnvironment() {
        String originalAppEnv = System.getProperty("APP_ENV");
        try {
            System.setProperty("APP_ENV", "test");

            assertEquals("test", ConfigRegistry.getAppEnv());
        } finally {
            if (originalAppEnv == null) {
                System.clearProperty("APP_ENV");
            } else {
                System.setProperty("APP_ENV", originalAppEnv);
            }
        }
    }
}
