package com.company.qa.unified.contracts;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.config.EnvironmentType;
import com.company.qa.unified.utils.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import au.com.dius.pact.provider.junit5.*;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.*;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Base class for Pact Provider verification.
 *
 * How to use:
 *  - Extend this class in each provider verification test
 *  - Annotate subclass with @Provider("service-name")
 *  - Ensure provider base URL is configured via env-config.json
 *
 * Safety:
 *  - Provider verification is blocked in PROD unless explicitly allowed
 */
@ExtendWith(PactVerificationInvocationContextProvider.class)
@PactBroker(
        url = "#{systemProperties['pact.broker.url']}",
        authentication = @PactBrokerAuth(
                token = "#{systemProperties['pact.broker.token']}"
        )
)
public abstract class ProviderVerifier {

    protected static final Log log = Log.get(ProviderVerifier.class);

    protected static final EnvironmentConfig ENV = EnvironmentConfig.get();
    protected static final EnvironmentType ENV_TYPE = EnvironmentType.current();

    /**
     * Subclasses must return the base URL of the provider under test.
     * Example: https://lookup.qa.company.com
     */
    protected abstract String providerBaseUrl();

    /**
     * Optional: allow provider verification in PROD
     * (default false, strongly discouraged)
     */
    protected boolean allowProdVerification() {
        return false;
    }

    /* =========================================================
       GLOBAL SETUP
       ========================================================= */

    @BeforeAll
    static void beforeAll() {
        PactConfig.logSummary();

        if (ENV_TYPE.isProd()) {
            log.warn("⚠️ Running Pact provider verification in PROD");
        }
    }

    /* =========================================================
       PER-INTERACTION SETUP
       ========================================================= */

    @BeforeEach
    void beforeEach(PactVerificationContext context) {

        if (context == null) {
            return;
        }

        // Safety guard
        if (ENV_TYPE.isProd() && !allowProdVerification()) {
            fail("❌ Pact provider verification is not allowed in PROD");
        }

        // Set provider target dynamically
        context.setTarget(
                new HttpTestTarget(
                        providerBaseUrl()
                )
        );

        // Add request filters (auth, headers, tracing)
        context.addRequestFilter((request, executionContext) -> {
            request.addHeader("X-Correlation-Id", UUID.randomUUID().toString());
            request.addHeader("User-Agent", "pact-provider-verifier");

            // Optional auth header from credentials
            String token = ENV.getOptional("provider.authToken");
            if (token != null && !token.isBlank()) {
                request.addHeader("Authorization", "Bearer " + token);
            }

            return request;
        });

        log.info("🔍 Verifying Pact interaction: {}",
                context.getInteraction().getDescription());
    }

    /* =========================================================
       PROVIDER STATE MANAGEMENT
       ========================================================= */

    /**
     * Default provider state handler.
     * Subclasses can override or add more @State methods.
     */
    @State("provider is healthy")
    public void providerIsHealthy(Map<String, Object> params) {
        log.info("Setting provider state: provider is healthy");
        // No-op by default
        // Subclasses may:
        // - seed DB
        // - mock downstreams
        // - reset caches
    }

    /* =========================================================
       VERIFICATION TEMPLATE
       ========================================================= */

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }
}
