package com.company.qa.unified.data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Factory for generating search-related test data.
 *
 * Covers:
 * - Phone number lookup
 * - Name search
 * - Business search
 * - Spam search
 * - Localization & language variants
 * - Edge cases & abuse scenarios
 *
 * This factory ONLY creates data.
 * It does NOT perform search execution.
 */
public final class SearchDataFactory {

    private SearchDataFactory() {
        // utility
    }

    /* =========================================================
       PHONE NUMBER SEARCH
       ========================================================= */

    public static SearchRequest validPhoneLookup() {
        return SearchRequest.builder()
                .query("+911234567890")
                .type(SearchType.PHONE)
                .countryCode("IN")
                .expectedResult(SearchExpectation.VERIFIED_USER)
                .build();
    }

    public static SearchRequest spamPhoneLookup() {
        return SearchRequest.builder()
                .query("+919999999999")
                .type(SearchType.PHONE)
                .countryCode("IN")
                .expectedResult(SearchExpectation.SPAM)
                .build();
    }

    public static SearchRequest unknownPhoneLookup() {
        return SearchRequest.builder()
                .query("+918888888888")
                .type(SearchType.PHONE)
                .countryCode("IN")
                .expectedResult(SearchExpectation.NOT_FOUND)
                .build();
    }

    /* =========================================================
       NAME SEARCH
       ========================================================= */

    public static SearchRequest commonNameSearch() {
        return SearchRequest.builder()
                .query("Amit Sharma")
                .type(SearchType.NAME)
                .countryCode("IN")
                .expectedResult(SearchExpectation.MULTIPLE_RESULTS)
                .build();
    }

    public static SearchRequest rareNameSearch() {
        return SearchRequest.builder()
                .query("Xyron Quillstone")
                .type(SearchType.NAME)
                .countryCode("US")
                .expectedResult(SearchExpectation.SINGLE_RESULT)
                .build();
    }

    /* =========================================================
       BUSINESS SEARCH
       ========================================================= */

    public static SearchRequest businessSearch() {
        return SearchRequest.builder()
                .query("Amazon Customer Care")
                .type(SearchType.BUSINESS)
                .countryCode("IN")
                .expectedResult(SearchExpectation.BUSINESS_PROFILE)
                .build();
    }

    public static SearchRequest verifiedBusinessSearch() {
        return SearchRequest.builder()
                .query("Truecaller Support")
                .type(SearchType.BUSINESS)
                .countryCode("GLOBAL")
                .expectedResult(SearchExpectation.VERIFIED_BUSINESS)
                .build();
    }

    /* =========================================================
       LOCALIZATION / LANGUAGE
       ========================================================= */

    public static SearchRequest hindiNameSearch() {
        return SearchRequest.builder()
                .query("मोहन")
                .type(SearchType.NAME)
                .language("hi")
                .countryCode("IN")
                .expectedResult(SearchExpectation.MULTIPLE_RESULTS)
                .build();
    }

    public static SearchRequest arabicBusinessSearch() {
        return SearchRequest.builder()
                .query("خدمة العملاء")
                .type(SearchType.BUSINESS)
                .language("ar")
                .countryCode("AE")
                .expectedResult(SearchExpectation.MULTIPLE_RESULTS)
                .build();
    }

    /* =========================================================
       NEGATIVE / ABUSE CASES
       ========================================================= */

    public static SearchRequest sqlInjectionAttempt() {
        return SearchRequest.builder()
                .query("' OR 1=1 --")
                .type(SearchType.NAME)
                .expectedResult(SearchExpectation.REJECTED)
                .build();
    }

    public static SearchRequest scriptInjectionAttempt() {
        return SearchRequest.builder()
                .query("<script>alert('xss')</script>")
                .type(SearchType.NAME)
                .expectedResult(SearchExpectation.REJECTED)
                .build();
    }

    public static SearchRequest extremelyLongQuery() {
        return SearchRequest.builder()
                .query("A".repeat(5000))
                .type(SearchType.NAME)
                .expectedResult(SearchExpectation.REJECTED)
                .build();
    }

    /* =========================================================
       PERFORMANCE / LOAD
       ========================================================= */

    public static List<SearchRequest> bulkSearchRequests(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i ->
                        SearchRequest.builder()
                                .query("+91123" + String.format("%05d", i))
                                .type(SearchType.PHONE)
                                .countryCode("IN")
                                .requestId(UUID.randomUUID().toString())
                                .expectedResult(SearchExpectation.UNKNOWN)
                                .build()
                )
                .toList();
    }

    /* =========================================================
       SEARCH REQUEST MODEL
       ========================================================= */

    public enum SearchType {
        PHONE,
        NAME,
        BUSINESS
    }

    public enum SearchExpectation {
        VERIFIED_USER,
        VERIFIED_BUSINESS,
        BUSINESS_PROFILE,
        SPAM,
        NOT_FOUND,
        SINGLE_RESULT,
        MULTIPLE_RESULTS,
        UNKNOWN,
        REJECTED
    }

    /**
     * Immutable search request model.
     */
    public static final class SearchRequest {

        private final String requestId;
        private final String query;
        private final SearchType type;
        private final String countryCode;
        private final String language;
        private final SearchExpectation expectedResult;

        private SearchRequest(Builder b) {
            this.requestId = b.requestId;
            this.query = b.query;
            this.type = b.type;
            this.countryCode = b.countryCode;
            this.language = b.language;
            this.expectedResult = b.expectedResult;
        }

        public String requestId() { return requestId; }
        public String query() { return query; }
        public SearchType type() { return type; }
        public String countryCode() { return countryCode; }
        public String language() { return language; }
        public SearchExpectation expectedResult() { return expectedResult; }

        /**
         * Convert to API request payload.
         */
        public Map<String, Object> toApiPayload() {
            return Map.of(
                    "requestId", requestId,
                    "query", query,
                    "type", type.name(),
                    "countryCode", countryCode,
                    "language", language
            );
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String requestId = UUID.randomUUID().toString();
            private String query;
            private SearchType type;
            private String countryCode = "GLOBAL";
            private String language = "en";
            private SearchExpectation expectedResult = SearchExpectation.UNKNOWN;

            private Builder() {}

            public Builder requestId(String requestId) {
                this.requestId = requestId; return this;
            }

            public Builder query(String query) {
                this.query = query; return this;
            }

            public Builder type(SearchType type) {
                this.type = type; return this;
            }

            public Builder countryCode(String countryCode) {
                this.countryCode = countryCode; return this;
            }

            public Builder language(String language) {
                this.language = language; return this;
            }

            public Builder expectedResult(SearchExpectation expectation) {
                this.expectedResult = expectation; return this;
            }

            public SearchRequest build() {
                return new SearchRequest(this);
            }
        }
    }
}
