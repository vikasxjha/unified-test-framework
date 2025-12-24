package com.company.qa.unified.config;

import com.company.qa.unified.utils.JsonUtils;
import com.company.qa.unified.utils.Log;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Central environment configuration loader.
 *
 * Responsibilities:
 * - Load env-config.json
 * - Resolve active environment
 * - Expose typed config accessors
 * - Enforce PROD safety rules
 *
 * Access pattern:
 *   EnvironmentConfig.get().getApiBaseUrl();
 */
public final class EnvironmentConfig {

    private static final Log log = Log.get(EnvironmentConfig.class);

    private static final String CONFIG_FILE = "/env-config.json";
    private static final String ENV_PROPERTY = "env";
    private static final String DEFAULT_ENV = "QA";

    private static volatile EnvironmentConfig INSTANCE;

    private final String environmentName;
    private final Map<String, Object> envConfig;

    /* =========================================================
       SINGLETON
       ========================================================= */

    private EnvironmentConfig() {
        this.environmentName = resolveEnvironment();
        this.envConfig = loadEnvironmentConfig(environmentName);

        log.info("Loaded environment configuration: {}", environmentName);
    }

    public static EnvironmentConfig get() {
        if (INSTANCE == null) {
            synchronized (EnvironmentConfig.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EnvironmentConfig();
                }
            }
        }
        return INSTANCE;
    }

    /* =========================================================
       ENV RESOLUTION
       ========================================================= */

    private String resolveEnvironment() {
        String env = System.getProperty(ENV_PROPERTY, DEFAULT_ENV).trim();
        if (env.isEmpty()) {
            throw new IllegalStateException("Environment cannot be empty");
        }
        return env.toUpperCase();
    }

    /* =========================================================
       CONFIG LOADING
       ========================================================= */

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadEnvironmentConfig(String env) {

        try (InputStream is =
                     EnvironmentConfig.class.getResourceAsStream(CONFIG_FILE)) {

            if (is == null) {
                throw new IllegalStateException(
                        "env-config.json not found in classpath");
            }

            Map<String, Object> root =
                    JsonUtils.fromJson(is, Map.class);

            Object cfg = root.get(env);
            if (cfg == null) {
                throw new IllegalStateException(
                        "No configuration found for environment: " + env);
            }

            return (Map<String, Object>) cfg;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load environment config", e);
        }
    }

    /* =========================================================
       BASIC ENV INFO
       ========================================================= */

    public String getEnvironmentName() {
        return environmentName;
    }

    public boolean isProd() {
        return "PROD".equalsIgnoreCase(environmentName);
    }

    /* =========================================================
       WEB / API
       ========================================================= */

    public String getWebBaseUrl() {
        return getRequired("webBaseUrl");
    }

    public String getApiBaseUrl() {
        return getRequired("apiBaseUrl");
    }

    /* =========================================================
       DATABASE
       ========================================================= */

    public String getDbUrl() {
        return getOptional("db.url");
    }

    public String getDbUser() {
        return getOptional("db.user");
    }

    public String getDbPassword() {
        return getOptional("db.password");
    }

    /* =========================================================
       KAFKA
       ========================================================= */

    public String getKafkaBootstrapServers() {
        return getOptional("kafka.bootstrapServers");
    }

    /* =========================================================
       METRICS
       ========================================================= */

    public String getMetricsEndpoint() {
        return getOptional("metrics.endpoint");
    }

    /* =========================================================
       CHAOS
       ========================================================= */

    public String getChaosEndpoint() {
        return getOptional("chaos.endpoint");
    }

    /* =========================================================
       CREDENTIALS (NON-PROD ONLY)
       ========================================================= */

    @SuppressWarnings("unchecked")
    public Map<String, String> getCredentials() {
        Object creds = envConfig.get("credentials");
        if (creds instanceof Map) {
            return (Map<String, String>) creds;
        }
        return Collections.emptyMap();
    }

    /* =========================================================
       INTERNAL HELPERS
       ========================================================= */

    private String getRequired(String key) {
        String value = getOptional(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required config key: " + key +
                            " for env: " + environmentName);
        }
        return value;
    }

    private String getOptional(String key) {
        Object value = resolveNestedKey(key);
        return value == null ? null : value.toString();
    }

    @SuppressWarnings("unchecked")
    private Object resolveNestedKey(String key) {
        String[] parts = key.split("\\.");
        Object current = envConfig;

        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<String, Object>) current).get(part);
        }
        return current;
    }

    /* =========================================================
       DEBUGGING
       ========================================================= */

    @Override
    public String toString() {
        return "EnvironmentConfig{" +
                "environmentName='" + environmentName + '\'' +
                '}';
    }
}
