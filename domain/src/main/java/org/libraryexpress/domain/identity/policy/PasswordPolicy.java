package org.libraryexpress.domain.identity.policy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** NIST SP 800-63B style: length limits and a denylist, no composition rules. */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 12;
    public static final int MAX_LENGTH = 128;

    private PasswordPolicy() {}

    public static boolean isDenied(String normalizedPassword) {
        return Denylist.ENTRIES.contains(normalizedPassword.toLowerCase(Locale.ROOT));
    }

    /** Lazy holder: the file is read once, on first use. */
    private static final class Denylist {
        static final Set<String> ENTRIES = load();

        private static Set<String> load() {
            try (InputStream in = PasswordPolicy.class.getResourceAsStream("/password-denylist.txt")) {
                if (in == null) throw new IllegalStateException("password-denylist.txt not found on the classpath");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    return reader.lines()
                            .map(String::strip)
                            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                            .map(line -> line.toLowerCase(Locale.ROOT))
                            .collect(Collectors.toUnmodifiableSet());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
