package com.company.qa.unified.contracts;

import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.config.EnvironmentType;
import com.company.qa.unified.utils.Log;

import au.com.dius.pact.provider.junit5.*;
import au.com.dius.pact.provider.junitsupport.*;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Base class for Pact Provider verification.
 */
@Provider("provider-name") // overridden by subclasses
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
     * Subclasses must return base URL.
     * Example: https://lookup.qa.company.com
     */
    protected abstract String providerBaseUrl();

    /**
     * Override ONLY if you really want PROD verification.
     */
    protected boolean allowProdVerification() {
        return false;
    }

    /* =========================================================
       GLOBAL SETUP
       ========================================================= */

    @BeforeAll
    static void beforeAll() {
        log.info("Starting Pact Provider Verification");

        if (ENV_TYPE.isProd()) {
            log.warn("⚠️ Running Pact provider verification in PROD");
        }
    }

    /* =========================================================
       PER-INTERACTION SETUP
       ========================================================= */

    @BeforeEach
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void beforeEach(PactVerificationContext context) {

        if (context == null) {
            return;
        }

        // Safety guard
        if (ENV_TYPE.isProd() && !allowProdVerification()) {
            fail("❌ Pact provider verification is not allowed in PROD");
        }

        URI baseUri = URI.create(providerBaseUrl());

        int port = baseUri.getPort() == -1
                ? baseUri.getScheme().equals("https") ? 443 : 80
                : baseUri.getPort();

        context.setTarget(
                new HttpTestTarget(
                        baseUri.getHost(),
                        port,
                        baseUri.getScheme()
                )
        );

        log.info(
                "🔍 Verifying Pact interaction: {}",
                context.getInteraction().getDescription()
        );
    }

    /* =========================================================
       PROVIDER STATES
       ========================================================= */

    @State("provider is healthy")
    public void providerIsHealthy(Map<String, Object> params) {
        log.info("Setting provider state: provider is healthy");
        // No-op by default
    }

    /* =========================================================
       VERIFICATION
       ========================================================= */

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }
}
