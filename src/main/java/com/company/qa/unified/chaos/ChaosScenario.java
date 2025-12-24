package com.company.qa.unified.chaos;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable description of a chaos experiment.
 *
 * This class ONLY models:
 * - what chaos to inject
 * - where to inject
 * - how long it lasts
 *
 * It does NOT:
 * - execute chaos
 * - talk to infrastructure
 * - know about environments
 *
 * This separation is critical for safety.
 */
public final class ChaosScenario {

    private final ChaosType type;
    private final String targetService;
    private final Duration duration;
    private final Map<String, Object> parameters;

    private ChaosScenario(
            ChaosType type,
            String targetService,
            Duration duration,
            Map<String, Object> parameters
    ) {
        this.type = type;
        this.targetService = targetService;
        this.duration = duration;
        this.parameters = parameters;
    }

    /* =========================================================
       FACTORY METHODS (ONLY way to create scenarios)
       ========================================================= */

    public static ChaosScenario latency(
            String service,
            Duration latency,
            Duration duration
    ) {
        validate(service, latency, duration);

        return new ChaosScenario(
                ChaosType.LATENCY,
                service,
                duration,
                Map.of(
                        "latencyMs", latency.toMillis()
                )
        );
    }

    public static ChaosScenario httpError(
            String service,
            int httpStatus,
            int percentage,
            Duration duration
    ) {
        validate(service, duration);

        if (httpStatus < 400 || httpStatus > 599) {
            throw new IllegalArgumentException(
                    "HTTP status must be between 400 and 599");
        }

        if (percentage < 1 || percentage > 100) {
            throw new IllegalArgumentException(
                    "Error percentage must be between 1 and 100");
        }

        return new ChaosScenario(
                ChaosType.HTTP_ERROR,
                service,
                duration,
                Map.of(
                        "statusCode", httpStatus,
                        "percentage", percentage
                )
        );
    }

    public static ChaosScenario kill(
            String service,
            Duration duration
    ) {
        validate(service, duration);

        return new ChaosScenario(
                ChaosType.KILL,
                service,
                duration,
                Collections.emptyMap()
        );
    }

    public static ChaosScenario networkIsolation(
            String service,
            Duration duration
    ) {
        validate(service, duration);

        return new ChaosScenario(
                ChaosType.NETWORK_ISOLATION,
                service,
                duration,
                Collections.emptyMap()
        );
    }

    /* =========================================================
       VALIDATION
       ========================================================= */

    private static void validate(
            String service,
            Duration duration
    ) {
        Objects.requireNonNull(service, "Target service cannot be null");
        Objects.requireNonNull(duration, "Duration cannot be null");

        if (service.isBlank()) {
            throw new IllegalArgumentException("Target service cannot be blank");
        }

        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
    }

    private static void validate(
            String service,
            Duration latency,
            Duration duration
    ) {
        validate(service, duration);

        Objects.requireNonNull(latency, "Latency cannot be null");

        if (latency.isZero() || latency.isNegative()) {
            throw new IllegalArgumentException("Latency must be positive");
        }
    }

    /* =========================================================
       SERIALIZATION (for ChaosOrchestrator / HTTP clients)
       ========================================================= */

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("type", type.name());
        map.put("targetService", targetService);
        map.put("durationMs", duration.toMillis());
        map.put("parameters", parameters);

        return map;
    }

    /* =========================================================
       GETTERS
       ========================================================= */

    public ChaosType getType() {
        return type;
    }

    public String getTargetService() {
        return targetService;
    }

    public Duration getDuration() {
        return duration;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    /* =========================================================
       TO STRING (AUDIT LOGS)
       ========================================================= */

    @Override
    public String toString() {
        return """
               ChaosScenario {
                 type=%s
                 targetService=%s
                 duration=%s
                 parameters=%s
               }
               """.formatted(
                type,
                targetService,
                duration,
                parameters
        );
    }

    /* =========================================================
       CHAOS TYPES
       ========================================================= */

    public enum ChaosType {
        LATENCY,
        HTTP_ERROR,
        KILL,
        NETWORK_ISOLATION
    }
}
