package org.libraryexpress.infrastructure.security;

import java.util.Base64;

/** Encodes and parses $argon2id$v=19$m=..,t=..,p=..$<salt>$<hash> (Base64 without padding). */
final class PhcFormat {

    private static final Base64.Encoder ENCODER = Base64.getEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getDecoder(); // padding is optional on decode

    record Parsed(int version, int memoryKib, int iterations, int parallelism, byte[] salt, byte[] hash) {}

    private PhcFormat() {}

    static String encode(int memoryKib, int iterations, int parallelism, byte[] salt, byte[] hash) {
        return "$argon2id$v=" + Argon2Settings.VERSION
                + "$m=" + memoryKib + ",t=" + iterations + ",p=" + parallelism
                + "$" + ENCODER.encodeToString(salt)
                + "$" + ENCODER.encodeToString(hash);
    }

    /** @throws IllegalArgumentException when the string is not a well-formed Argon2id PHC string */
    static Parsed parse(String phc) {
        try {
            String[] parts = phc.split("\\$");
            if (parts.length != 6 || !parts[0].isEmpty() || !"argon2id".equals(parts[1]))
                throw new IllegalArgumentException("Unexpected PHC structure");

            String[] params = parts[3].split(",");
            if (params.length != 3) throw new IllegalArgumentException("Unexpected PHC parameters");

            return new Parsed(
                    Integer.parseInt(withoutPrefix(parts[2], "v=")),
                    Integer.parseInt(withoutPrefix(params[0], "m=")),
                    Integer.parseInt(withoutPrefix(params[1], "t=")),
                    Integer.parseInt(withoutPrefix(params[2], "p=")),
                    DECODER.decode(parts[4]),
                    DECODER.decode(parts[5]));
        } catch (IllegalArgumentException e) { // includes NumberFormatException and bad Base64
            throw new IllegalArgumentException("Malformed Argon2id PHC string", e);
        }
    }

    private static String withoutPrefix(String value, String prefix) {
        if (!value.startsWith(prefix)) throw new IllegalArgumentException("Missing " + prefix);
        return value.substring(prefix.length());
    }
}
