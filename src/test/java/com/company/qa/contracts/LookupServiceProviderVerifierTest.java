package com.company.qa.contracts;

import au.com.dius.pact.provider.junitsupport.Provider;
import com.company.qa.unified.contracts.ProviderVerifier;
import org.junit.jupiter.api.TestInstance;

/**
 * Pact provider verification for Lookup Service.
 *
 * This test verifies that the Lookup Service provider
 * fulfills the contracts defined by its consumers.
 */
@Provider("lookup-service")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LookupServiceProviderVerifierTest extends ProviderVerifier {

    @Override
    protected String providerBaseUrl() {
        return ENV.getApiBaseUrl() + "/lookup";
    }

    @Override
    protected boolean allowProdVerification() {
        // Only allow verification in non-PROD environments
        return false;
    }
}

