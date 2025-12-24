package com.company.qa.unified.config;

import com.company.qa.unified.utils.Log;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Central credential provider for the automation framework.
 *
 * Priority order:
 * 1. System properties (-Dkey=value)
 * 2. Environment variables
 * 3. env-config.json (LAST resort – non-prod only)
 *
 * NEVER log secret values.
 */
public final class CredentialsProvider {

    private static final Log log = Log.get(CredentialsProvider.class);

    private static final EnvironmentConfig ENV = EnvironmentConfig.get();

    private CredentialsProvider() {
        // utility
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    /**
     * Get username/password credentials by logical name.
     *
     * Example keys:
     * - user.free
     * - user.premium
     * - admin
     */
    public static Credentials getCredentials(String key) {
        Objects.requireNonNull(key, "Credential key cannot be null");

        String usernameKey = key + ".username";
        String passwordKey = key + ".password";

        String username = resolveSecret(usernameKey)
                .orElseThrow(() -> missing(usernameKey));

        String password = resolveSecret(passwordKey)
                .orElseThrow(() -> missing(passwordKey));

        log.debug("Credentials loaded for key={}", key);
        return new Credentials(username, password);
    }

    /**
     * Get a single secret (token, api-key, etc).
     */
    public static String getSecret(String key) {
        return resolveSecret(key)
                .orElseThrow(() -> missing(key));
    }

    /* =========================================================
       RESOLUTION STRATEGY
       ========================================================= */

    private static Optional<String> resolveSecret(String key) {

        // 1️⃣ JVM system property
        String sysProp = System.getProperty(key);
        if (isPresent(sysProp)) {
            return Optional.of(sysProp);
        }

        // 2️⃣ Environment variable (KEY → KEY)
        String envVar = System.getenv(toEnvVar(key));
        if (isPresent(envVar)) {
            return Optional.of(envVar);
        }

        // 3️⃣ env-config.json (non-prod only)
        if (!ENV.isProd()) {
            Map<String, String> creds = ENV.getCredentials();
            if (creds != null && isPresent(creds.get(key))) {
                return Optional.of(creds.get(key));
            }
        }

        return Optional.empty();
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private static IllegalStateException missing(String key) {
        return new IllegalStateException(
                "Missing credential: " + key +
                        "\nProvide via -D" + key +
                        " or env var " + toEnvVar(key)
        );
    }

    private static String toEnvVar(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    /* =========================================================
       CREDENTIAL MODEL
       ========================================================= */

    public static final class Credentials {
        private final String username;
        private final String password;

        private Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String username() {
            return username;
        }

        public String password() {
            return password;
        }

        @Override
        public String toString() {
            return "Credentials{username='%s', password='********'}"
                    .formatted(username);
        }
    }
}
