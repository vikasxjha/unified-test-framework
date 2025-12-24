package com.company.qa.unified.pages.api;

import com.company.qa.unified.drivers.APIClientFactory;
import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * PhoneLookupApi
 *
 * Encapsulates phone number lookup APIs.
 *
 * Covers:
 * - Single phone lookup
 * - Bulk lookup
 * - Spam confidence / tags
 * - Cache behavior (ETag / If-None-Match)
 * - Consent-aware lookups
 * - Admin / debug endpoints
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use PhoneLookupApi
 */
public class PhoneLookupApi extends BaseApiClient {

    private static final Log log =
            Log.get(PhoneLookupApi.class);

    /* =========================================================
       SINGLE LOOKUP
       ========================================================= */

    /**
     * Perform a phone number lookup.
     */
    public Response lookup(
            String accessToken,
            String phoneNumber,
            String countryCode
    ) {

        log.info("📞 Lookup phone={} country={}",
                mask(phoneNumber), countryCode);

        Response response =
                get(
                        authenticated(accessToken),
                        "/lookup?phone=" + phoneNumber
                                + "&countryCode=" + countryCode
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Lookup without authentication (limited data).
     */
    public Response anonymousLookup(
            String phoneNumber,
            String countryCode
    ) {

        log.info("📞 Anonymous lookup phone={} country={}",
                mask(phoneNumber), countryCode);

        Response response =
                get(
                        unauthenticated(),
                        "/lookup/anonymous?phone=" + phoneNumber
                                + "&countryCode=" + countryCode
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       BULK LOOKUP
       ========================================================= */

    /**
     * Perform bulk lookup.
     */
    public Response bulkLookup(
            String accessToken,
            List<String> phoneNumbers,
            String countryCode
    ) {

        log.info("📞 Bulk lookup count={} country={}",
                phoneNumbers.size(), countryCode);

        Response response =
                post(
                        authenticated(accessToken),
                        "/lookup/bulk",
                        Map.of(
                                "phones", phoneNumbers,
                                "countryCode", countryCode
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       SPAM / CONFIDENCE
       ========================================================= */

    /**
     * Fetch spam confidence & tags.
     */
    public Response spamConfidence(
            String accessToken,
            String phoneNumber,
            String countryCode
    ) {

        log.info("🚨 Spam confidence phone={}",
                mask(phoneNumber));

        Response response =
                get(
                        authenticated(accessToken),
                        "/lookup/spam?phone=" + phoneNumber
                                + "&countryCode=" + countryCode
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       CACHE / CONDITIONAL REQUESTS
       ========================================================= */

    /**
     * Perform lookup with ETag for cache validation.
     */
    public Response lookupWithEtag(
            String accessToken,
            String phoneNumber,
            String countryCode,
            String etag
    ) {

        log.info("🗄 Conditional lookup with ETag phone={}",
                mask(phoneNumber));

        Response response =
                APIClientFactory
                        .customClient(Map.of(
                                "Authorization",
                                "Bearer " + accessToken,
                                "If-None-Match",
                                etag
                        ))
                        .get(
                                "/lookup?phone=" + phoneNumber
                                        + "&countryCode=" + countryCode
                        );

        // 200 (fresh) OR 304 (cached)
        if (response.statusCode() != 200 &&
                response.statusCode() != 304) {

            fail("Unexpected status for ETag lookup: "
                    + response.statusCode());
        }

        return response;
    }

    /* =========================================================
       CONSENT / PRIVACY
       ========================================================= */

    /**
     * Lookup respecting GDPR / consent flags.
     */
    public Response lookupWithConsent(
            String accessToken,
            String phoneNumber,
            String countryCode,
            boolean consentGranted
    ) {

        log.info("🛡 Consent-aware lookup phone={} consent={}",
                mask(phoneNumber), consentGranted);

        Response response =
                post(
                        authenticated(accessToken),
                        "/lookup/consent",
                        Map.of(
                                "phone", phoneNumber,
                                "countryCode", countryCode,
                                "consent", consentGranted
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ADMIN / DEBUG
       ========================================================= */

    /**
     * Fetch raw lookup data (admin/debug).
     */
    public Response adminRawLookup(
            String phoneNumber,
            String countryCode
    ) {

        log.info("🛠 Admin raw lookup phone={}",
                mask(phoneNumber));

        Response response =
                get(
                        admin(),
                        "/admin/lookup/raw?phone=" + phoneNumber
                                + "&countryCode=" + countryCode
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public static void assertNamePresent(Response response) {

        String name = response.path("name");
        if (name == null || name.isBlank()) {
            fail("Expected name in lookup response but found none");
        }
    }

    public static void assertSpamScoreAbove(
            Response response,
            double minScore
    ) {

        Double score = response.path("spam.score");
        if (score == null || score < minScore) {
            fail("Spam score below expected: " + score);
        }
    }

    public static void assertLookupEmpty(Response response) {

        Boolean exists = response.path("exists");
        if (Boolean.TRUE.equals(exists)) {
            fail("Expected no lookup result but record exists");
        }
    }

    /* =========================================================
       UTILITIES
       ========================================================= */

    private String mask(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }
}
