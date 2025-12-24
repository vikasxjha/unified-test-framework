package com.company.qa.unified.data;

import com.company.qa.unified.utils.JsonUtils;
import com.company.qa.unified.utils.Log;

import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Golden (canonical) datasets.
 *
 * Golden datasets represent:
 * - Known-good API responses
 * - Stable DB snapshots
 * - Canonical event payloads
 * - Regression-safe reference data
 *
 * Design goals:
 * - Deterministic
 * - Immutable
 * - Versioned
 * - Auditable
 *
 * Golden data MUST NOT be modified at runtime.
 */
public final class GoldenDatasetRegistry {

    private static final Log log =
            Log.get(GoldenDatasetRegistry.class);

    private static final String DATASET_ROOT =
            "/golden-datasets/";

    /**
     * Cache to avoid repeated disk reads.
     */
    private static final Map<String, GoldenDataset> CACHE =
            new ConcurrentHashMap<>();

    private GoldenDatasetRegistry() {
        // utility
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    /**
     * Load a golden dataset by logical name.
     *
     * Example:
     *   GoldenDataset dataset =
     *     GoldenDatasetRegistry.get("lookup.basic.success");
     */
    public static GoldenDataset get(String datasetKey) {
        Objects.requireNonNull(datasetKey, "datasetKey cannot be null");

        return CACHE.computeIfAbsent(
                datasetKey,
                GoldenDatasetRegistry::loadDataset
        );
    }

    /**
     * Convenience method to fetch just the payload.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getPayload(String datasetKey) {
        return get(datasetKey).payload();
    }

    /**
     * Check if a golden dataset exists.
     */
    public static boolean exists(String datasetKey) {
        return GoldenDatasetRegistry.class
                .getResource(DATASET_ROOT + datasetKey + ".json") != null;
    }

    /* =========================================================
       LOADING LOGIC
       ========================================================= */

    @SuppressWarnings("unchecked")
    private static GoldenDataset loadDataset(String key) {

        String path = DATASET_ROOT + key + ".json";

        try (InputStream is =
                     GoldenDatasetRegistry.class.getResourceAsStream(path)) {

            if (is == null) {
                throw new IllegalStateException(
                        "Golden dataset not found: " + path);
            }

            Map<String, Object> raw =
                    JsonUtils.fromJson(is, Map.class);

            String version =
                    String.valueOf(raw.getOrDefault("version", "v1"));

            String description =
                    String.valueOf(raw.getOrDefault(
                            "description", "No description"));

            Object payloadObj = raw.get("payload");
            if (!(payloadObj instanceof Map)) {
                throw new IllegalStateException(
                        "Golden dataset payload must be a JSON object");
            }

            Map<String, Object> payload =
                    Collections.unmodifiableMap(
                            (Map<String, Object>) payloadObj
                    );

            GoldenDataset dataset =
                    new GoldenDataset(
                            key,
                            version,
                            description,
                            payload,
                            Instant.now()
                    );

            log.info("Loaded golden dataset key={} version={}",
                    key, version);

            return dataset;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load golden dataset: " + key, e);
        }
    }

    /* =========================================================
       GOLDEN DATASET MODEL
       ========================================================= */

    /**
     * Immutable representation of a golden dataset.
     */
    public static final class GoldenDataset {

        private final String key;
        private final String version;
        private final String description;
        private final Map<String, Object> payload;
        private final Instant loadedAt;

        private GoldenDataset(
                String key,
                String version,
                String description,
                Map<String, Object> payload,
                Instant loadedAt
        ) {
            this.key = key;
            this.version = version;
            this.description = description;
            this.payload = payload;
            this.loadedAt = loadedAt;
        }

        public String key() {
            return key;
        }

        public String version() {
            return version;
        }

        public String description() {
            return description;
        }

        /**
         * Canonical payload.
         * This is immutable and safe to share across tests.
         */
        public Map<String, Object> payload() {
            return payload;
        }

        public Instant loadedAt() {
            return loadedAt;
        }

        @Override
        public String toString() {
            return """
                   GoldenDataset {
                     key='%s'
                     version='%s'
                     description='%s'
                   }
                   """.formatted(key, version, description);
        }
    }
}
