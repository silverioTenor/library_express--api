package org.libraryexpress.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.UnitTest;

@UnitTest
@DisplayName("ConfigRegistry - Security & Fail-Fast Unit Test")
class ConfigRegistryTest {

    @Test
    @DisplayName(
            "Should trigger a fatal IllegalStateException boundary when evaluating a missing mandatory environment token key")
    void shouldFailFast_whenMandatoryKeyIsCompletelyAbsent() throws Exception {
        // Using reflection to invoke the private cascade core helper logic cleanly
        Method resolveMethod = ConfigRegistry.class.getDeclaredMethod("resolve", String.class, String.class);
        resolveMethod.setAccessible(true);

        // Act & Assert: Simulating an environment call for an unregistered key with zero property failbacks and zero
        // defaults
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
    @DisplayName("Should resolve a system property when the configuration key is first requested")
    void shouldResolveFromSystemProperty_whenConfigurationKeyIsNotCached() throws Exception {
        String key =
                "CONFIG_REGISTRY_PRIORITY_TEST_" + UUID.randomUUID().toString().replace("-", "");
        try {
            System.setProperty(key, "system-property-value");

            assertEquals("system-property-value", invokeResolve(key, "default-value"));
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    @DisplayName("Should lazily resolve and cache a configuration value on its first lookup")
    void shouldCacheResolvedValue_whenConfigurationKeyIsRequestedMoreThanOnce() throws Exception {
        String key =
                "CONFIG_REGISTRY_CACHE_TEST_" + UUID.randomUUID().toString().replace("-", "");
        System.setProperty(key, "first-value");

        try {
            String firstValue = invokeResolve(key, "default-value");
            System.setProperty(key, "second-value");

            String cachedValue = invokeResolve(key, "default-value");

            assertEquals("first-value", firstValue);
            assertEquals("first-value", cachedValue);
        } finally {
            System.clearProperty(key);
        }
    }

    private String invokeResolve(String key, String defaultValue) throws Exception {
        Method resolveMethod = ConfigRegistry.class.getDeclaredMethod("resolve", String.class, String.class);
        resolveMethod.setAccessible(true);

        try {
            return (String) resolveMethod.invoke(null, key, defaultValue);
        } catch (Exception exception) {
            throw (Exception) exception.getCause();
        }
    }
}
