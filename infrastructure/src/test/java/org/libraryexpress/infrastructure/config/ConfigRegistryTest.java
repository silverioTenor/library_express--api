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
        Method resolveMethod = ConfigRegistry.class.getDeclaredMethod("resolve", String.class, String.class, String.class);
        resolveMethod.setAccessible(true);

        // Act & Assert: Simulating an environment call for an unregistered key with zero property failbacks and zero defaults
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            try {
                resolveMethod.invoke(null, "COMPLETELY_ABSENT_MANDATORY_KEY_802", null, null);
            } catch (Exception e) {
                throw e.getCause(); // Extracting the actual underlying target exception
            }
        });

        assertTrue(exception.getMessage().contains("is mandatory but was not defined"));
    }
}
