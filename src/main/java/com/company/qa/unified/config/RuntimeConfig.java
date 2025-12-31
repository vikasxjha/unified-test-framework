package com.company.qa.unified.config;

import com.company.qa.unified.utils.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * RuntimeConfig centralizes ALL runtime / execution-time flags.
 *
 * These are values that:
 * - change per run
 * - should NOT live in env-config.json
 * - are usually passed via CLI / CI
 *
 * Examples:
 *   mvn test -Denv=QA -Dbrowser=chromium -Dheadless=true -Dtags=@smoke
 */
public final class RuntimeConfig {

    private static final Log log = Log.get(RuntimeConfig.class);

    /* =========================================================
       JVM PROPERTY KEYS
       ========================================================= */

    private static final String ENV = "env";
    private static final String BROWSER = "browser";
    private static final String HEADLESS = "headless";
    private static final String TAGS = "tags";
    private static final String PARALLEL = "parallel";
    private static final String THREAD_COUNT = "threads";
    private static final String UPDATE_VISUAL_BASELINE = "updateVisualBaseline";
    private static final String RECORD_VIDEO = "recordVideo";
    private static final String RECORD_TRACE = "recordTrace";
    private static final String SLOW_MO = "slowMo";
    private static final String RETRY_COUNT = "retryCount";
    private static final String NOTIFICATIONS_ENABLED = "notifications.enabled";
    private static final String NOTIFICATIONS_ON_FAILURE = "notifications.onFailure";

    /* =========================================================
       AI SELF-HEALING CONFIGURATION
       ========================================================= */

    // Enable AI self-healing at runtime
    public static final boolean AI_SELF_HEALING_ENABLED =
            Boolean.parseBoolean(
                    System.getProperty("ai.self.heal",
                            System.getenv().getOrDefault("AI_SELF_HEAL", "false"))
            );

    // Minimum confidence required from AI
    public static final int AI_HEAL_CONFIDENCE_THRESHOLD =
            Integer.parseInt(
                    System.getProperty("ai.heal.confidence", "70")
            );

    // Enable healing only in CI
    public static final boolean AI_HEAL_ONLY_IN_CI =
            Boolean.parseBoolean(
                    System.getenv().getOrDefault("CI", "false")
            );

    /* =========================================================
       NOTIFICATION CONFIGURATION
       ========================================================= */

    // Enable notifications (email, Slack, etc.)
    public static final boolean NOTIFICATIONS_ENABLED =
            Boolean.parseBoolean(
                    System.getProperty("notifications.enabled",
                            String.valueOf(DEFAULT_NOTIFICATIONS_ENABLED))
            );

    // Send notifications on test failure only
    public static final boolean NOTIFICATIONS_ON_FAILURE =
            Boolean.parseBoolean(
                    System.getProperty("notifications.onFailure",
                            String.valueOf(DEFAULT_NOTIFICATIONS_ON_FAILURE))
            );

    /* =========================================================
       RETRY CONFIGURATION
       ========================================================= */

    // Maximum retry count for flaky tests
    public static final int MAX_RETRY_COUNT =
            Integer.parseInt(
                    System.getProperty("retryCount",
                            String.valueOf(DEFAULT_RETRY_COUNT))
            );

    /* =========================================================
       DEFAULTS
       ========================================================= */

    private static final String DEFAULT_BROWSER = "chromium";
    private static final boolean DEFAULT_HEADLESS = true;
    private static final boolean DEFAULT_PARALLEL = true;
    private static final int DEFAULT_THREAD_COUNT = 4;
    private static final int DEFAULT_SLOW_MO = 0;
    private static final int DEFAULT_RETRY_COUNT = 1;
    private static final boolean DEFAULT_NOTIFICATIONS_ENABLED = false;
    private static final boolean DEFAULT_NOTIFICATIONS_ON_FAILURE = true;

    private RuntimeConfig() {
        // utility
    }

    /* =========================================================
       ENVIRONMENT
       ========================================================= */

    public static EnvironmentType environment() {
        return EnvironmentType.current();
    }

    /* =========================================================
       BROWSER / UI EXECUTION
       ========================================================= */

    public static String browser() {
        return get(BROWSER, DEFAULT_BROWSER)
                .toLowerCase(Locale.ROOT);
    }

    public static boolean isHeadless() {
        return getBoolean(HEADLESS, DEFAULT_HEADLESS);
    }

    public static int slowMoMs() {
        return getInt(SLOW_MO, DEFAULT_SLOW_MO);
    }

    public static boolean recordVideo() {
        return getBoolean(RECORD_VIDEO, false);
    }

    public static boolean recordTrace() {
        return getBoolean(RECORD_TRACE, false);
    }

    /**
     * Get headless mode.
     */
    public static boolean headless() {
        return isHeadless();
    }

    /**
     * Check if tracing is enabled.
     */
    public static boolean enableTracing() {
        return recordTrace();
    }

    /**
     * Get UI timeout in seconds.
     */
    public static int uiTimeoutSeconds() {
        return getInt("uiTimeout", 30);
    }

    /**
     * Get environment as string.
     */
    public static String getEnvironment() {
        return environment().name();
    }

    /**
     * Get browser type.
     */
    public static String getBrowser() {
        return browser();
    }

    /* =========================================================
       TEST EXECUTION
       ========================================================= */

    public static boolean isParallel() {
        return getBoolean(PARALLEL, DEFAULT_PARALLEL);
    }

    public static int threadCount() {
        return getInt(THREAD_COUNT, DEFAULT_THREAD_COUNT);
    }

    public static List<String> cucumberTags() {
        return Optional.ofNullable(System.getProperty(TAGS))
                .map(t -> Arrays.stream(t.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList())
                .orElse(List.of());
    }

    /* =========================================================
       VISUAL TESTING
       ========================================================= */

    public static boolean updateVisualBaseline() {
        return getBoolean(UPDATE_VISUAL_BASELINE, false);
    }

    /* =========================================================
       INTERNAL HELPERS
       ========================================================= */

    private static String get(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(
                System.getProperty(key, String.valueOf(defaultValue))
        );
    }

    private static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(
                    System.getProperty(key, String.valueOf(defaultValue))
            );
        } catch (NumberFormatException e) {
            log.warn("Invalid integer for {}. Using default {}", key, defaultValue);
            return defaultValue;
        }
    }

    /* =========================================================
       DEBUG SNAPSHOT
       ========================================================= */

    public static void logRuntimeSummary() {
        log.info("""
                🧪 Runtime Configuration
                ------------------------
                Environment        : {}
                Browser            : {}
                Headless           : {}
                SlowMo (ms)        : {}
                Parallel           : {}
                Threads            : {}
                Cucumber Tags      : {}
                Update Baseline    : {}
                Record Video       : {}
                Record Trace       : {}
                """,
                environment(),
                browser(),
                isHeadless(),
                slowMoMs(),
                isParallel(),
                threadCount(),
                cucumberTags(),
                updateVisualBaseline(),
                recordVideo(),
                recordTrace()
        );
    }
}
