package org.fmazmz.authmanager;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads {@code .env} via <a href="https://github.com/cdimascio/dotenv-java">dotenv-java</a> before Spring
 * starts so {@code application.yaml} placeholders like {@code ${JWT_RSA_PRIVATE_KEY:}} resolve.
 * <p>
 * Precedence: real OS environment variables and existing JVM system properties are never overwritten.
 */
final class DotenvBootstrap {

    private DotenvBootstrap() {}

    static void apply() {
        Path dir = resolveDotenvDirectory();
        Dotenv dotenv =
                Dotenv.configure().directory(dir.toString()).ignoreIfMissing().load();
        for (DotenvEntry entry : dotenv.entries()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }
            if (System.getenv(key) != null) {
                continue;
            }
            if (System.getProperty(key) != null) {
                continue;
            }
            System.setProperty(key, entry.getValue());
        }
    }

    /**
     * Prefer {@code .env} next to the JVM working directory, then {@code auth-manager/.env} when the
     * process is started from the monorepo root.
     */
    private static Path resolveDotenvDirectory() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        if (Files.isRegularFile(cwd.resolve(".env"))) {
            return cwd;
        }
        Path nested = cwd.resolve("auth-manager");
        if (Files.isRegularFile(nested.resolve(".env"))) {
            return nested;
        }
        return cwd;
    }
}
