package org.libraryexpress.domain.core.util;

import java.util.UUID;

public abstract class RandomGenerator {

    public static String UUID() {
        return UUID.randomUUID().toString();
    }
}
