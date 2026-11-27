package org.libraryexpress.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.libraryexpress.domain.identity.security.PasswordHasher;
import org.libraryexpress.domain.identity.valueobject.PasswordHash;
import org.libraryexpress.domain.identity.valueobject.PlainPassword;

public class Argon2idPasswordHasher implements PasswordHasher {

    private final Argon2Settings settings;
    private final SecureRandom random = new SecureRandom();

    public Argon2idPasswordHasher(Argon2Settings settings) {
        this.settings = settings;
    }

    @Override
    public PasswordHash hash(PlainPassword password) {
        byte[] salt = new byte[Argon2Settings.SALT_LENGTH];
        random.nextBytes(salt);

        byte[] hash = derive(
                password,
                salt,
                settings.memoryKib(),
                settings.iterations(),
                settings.parallelism(),
                Argon2Settings.HASH_LENGTH);

        return PasswordHash.of(
                PhcFormat.encode(settings.memoryKib(), settings.iterations(), settings.parallelism(), salt, hash));
    }

    @Override
    public boolean verify(PlainPassword password, PasswordHash passwordHash) {
        PhcFormat.Parsed stored;
        try {
            stored = PhcFormat.parse(passwordHash.value());
        } catch (IllegalArgumentException e) {
            return false;
        }

        byte[] actual = derive(
                password,
                stored.salt(),
                stored.memoryKib(),
                stored.iterations(),
                stored.parallelism(),
                stored.hash().length);

        return MessageDigest.isEqual(actual, stored.hash()); // constant time
    }

    @Override
    public boolean needsRehash(PasswordHash passwordHash) {
        PhcFormat.Parsed stored;
        try {
            stored = PhcFormat.parse(passwordHash.value());
        } catch (IllegalArgumentException e) {
            return true;
        }

        return stored.version() != Argon2Settings.VERSION
                || stored.memoryKib() != settings.memoryKib()
                || stored.iterations() != settings.iterations()
                || stored.parallelism() != settings.parallelism()
                || stored.salt().length != Argon2Settings.SALT_LENGTH
                || stored.hash().length != Argon2Settings.HASH_LENGTH;
    }

    private byte[] derive(
            PlainPassword password, byte[] salt, int memoryKib, int iterations, int parallelism, int hashLength) {
        Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(memoryKib)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);

        byte[] passwordBytes = password.value().getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[hashLength];
        try {
            generator.generateBytes(passwordBytes, output);
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
        }
        return output;
    }
}
