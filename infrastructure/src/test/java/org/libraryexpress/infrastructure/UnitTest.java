package org.libraryexpress.infrastructure;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom meta-annotation to categorize integration tests within the infrastructure layer.
 * Leverages JUnit 5 Tag mechanism to avoid rigid base-class inheritance patterns.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("unit")
public @interface UnitTest {
}
