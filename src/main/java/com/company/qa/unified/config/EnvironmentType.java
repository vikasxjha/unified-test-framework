package com.company.qa.unified.config;

import java.util.Locale;

/**
 * Supported execution environments.
 *
 * Centralizing environment names avoids:
 * - String duplication
 * - Case sensitivity bugs
 * - Invalid env values at runtime
 *
 * Usage:
 *   EnvironmentType env = EnvironmentType.current();
 */
public enum EnvironmentType {

    QA,
    STAGE,
    PROD;

    /**
     * Resolve environment from system property.
     * Defaults to QA if not provided.
     */
    public static EnvironmentType current() {
        String env =
                System.getProperty("env", QA.name())
                        .toUpperCase(Locale.ROOT)
                        .trim();

        try {
            return EnvironmentType.valueOf(env);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid environment: " + env +
                            ". Supported values: QA, STAGE, PROD"
            );
        }
    }

    public boolean isProd() {
        return this == PROD;
    }

    public boolean isNonProd() {
        return this != PROD;
    }

    public boolean isQa() {
        return this == QA;
    }

    public boolean isStage() {
        return this == STAGE;
    }
}
