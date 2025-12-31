package com.company.qa.unified.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * ScenarioContext
 *
 * Thread-safe context storage for Cucumber scenarios.
 *
 * Responsibilities:
 * - Store scenario-level state
 * - Manage self-healing flags
 * - Share data between steps
 * - Provide thread-safe access
 *
 * IMPORTANT:
 * - Uses ThreadLocal for parallel execution
 * - Auto-cleans up after scenario
 * - No manual cleanup needed
 */
public final class ScenarioContext {

    private static final Log log = Log.get(ScenarioContext.class);

    private static final ThreadLocal<Map<String, Object>> CONTEXT =
            ThreadLocal.withInitial(HashMap::new);

    private static final ThreadLocal<Boolean> SELF_HEALING_ENABLED =
            ThreadLocal.withInitial(() -> false);

    private ScenarioContext() {
        // Utility class
    }

    /* =========================================================
       SELF-HEALING MANAGEMENT
       ========================================================= */

    /**
     * Enable or disable self-healing for current scenario.
     *
     * @param enabled true to enable self-healing
     */
    public static void enableSelfHealing(boolean enabled) {
        SELF_HEALING_ENABLED.set(enabled);
        log.info("Self-healing {} for current scenario",
                enabled ? "enabled" : "disabled");
    }

    /**
     * Check if self-healing is enabled for current scenario.
     *
     * @return true if self-healing is enabled
     */
    public static boolean isSelfHealingEnabled() {
        return SELF_HEALING_ENABLED.get();
    }

    /* =========================================================
       CONTEXT DATA STORAGE
       ========================================================= */

    /**
     * Store a value in scenario context.
     *
     * @param key   context key
     * @param value value to store
     */
    public static void set(String key, Object value) {
        CONTEXT.get().put(key, value);
        log.debug("Context set: {} = {}", key, value);
    }

    /**
     * Retrieve a value from scenario context.
     *
     * @param key context key
     * @param <T> expected type
     * @return stored value or null
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) CONTEXT.get().get(key);
    }

    /**
     * Retrieve a value with a default fallback.
     *
     * @param key          context key
     * @param defaultValue fallback value
     * @param <T>          expected type
     * @return stored value or default
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T defaultValue) {
        Object value = CONTEXT.get().get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Check if a key exists in context.
     *
     * @param key context key
     * @return true if key exists
     */
    public static boolean contains(String key) {
        return CONTEXT.get().containsKey(key);
    }

    /**
     * Remove a value from context.
     *
     * @param key context key
     */
    public static void remove(String key) {
        CONTEXT.get().remove(key);
        log.debug("Context removed: {}", key);
    }

    /* =========================================================
       CLEANUP
       ========================================================= */

    /**
     * Clear all scenario context data.
     * Called automatically by Cucumber hooks.
     */
    public static void clear() {
        CONTEXT.get().clear();
        SELF_HEALING_ENABLED.remove();
        log.debug("Scenario context cleared");
    }

    /**
     * Get current context size (for debugging).
     *
     * @return number of stored values
     */
    public static int size() {
        return CONTEXT.get().size();
    }
}


