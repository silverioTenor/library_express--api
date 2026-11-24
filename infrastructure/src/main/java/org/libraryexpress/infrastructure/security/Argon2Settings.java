package org.libraryexpress.infrastructure.security;

public record Argon2Settings(int memoryKib, int iterations, int parallelism) {

    public static final int VERSION = 19; // 0x13
    public static final int SALT_LENGTH = 16;
    public static final int HASH_LENGTH = 32;

    public Argon2Settings {
        if (iterations < 1 || parallelism < 1)
            throw new IllegalArgumentException("Argon2 iterations and parallelism must be positive");
        if (memoryKib < 8 * parallelism)
            throw new IllegalArgumentException("Argon2 memory must be at least 8 KiB per lane");
    }

    /** OWASP baseline: m = 19 MiB, t = 2, p = 1. */
    public static Argon2Settings owaspDefaults() {
        return new Argon2Settings(19_456, 2, 1);
    }
}