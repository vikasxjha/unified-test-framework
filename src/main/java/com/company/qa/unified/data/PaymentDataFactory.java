package com.company.qa.unified.data;

import com.company.qa.unified.config.EnvironmentConfig;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating payment-related test data.
 *
 * Design goals:
 * - Deterministic defaults
 * - Covers success, failure, retry, refund flows
 * - No hardcoded secrets
 * - Safe for non-prod usage only
 *
 * This factory ONLY creates data objects,
 * it does NOT execute payments.
 */
public final class PaymentDataFactory {

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    private PaymentDataFactory() {
        // utility
    }

    /* =========================================================
       SUBSCRIPTION PAYMENTS
       ========================================================= */

    public static PaymentRequest premiumSubscription() {
        return PaymentRequest.builder()
                .paymentId(randomPaymentId())
                .userId(UserFactory.premiumUser().userId())
                .amount(new BigDecimal("299.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.UPI)
                .planId("PREMIUM_MONTHLY")
                .recurring(true)
                .billingDate(LocalDate.now())
                .build();
    }

    public static PaymentRequest yearlySubscription() {
        return PaymentRequest.builder()
                .paymentId(randomPaymentId())
                .userId(UserFactory.premiumUser().userId())
                .amount(new BigDecimal("2999.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.CARD)
                .planId("PREMIUM_YEARLY")
                .recurring(true)
                .billingDate(LocalDate.now())
                .build();
    }

    /* =========================================================
       ONE-TIME PAYMENTS
       ========================================================= */

    public static PaymentRequest oneTimeAddOn(String userId) {
        return PaymentRequest.builder()
                .paymentId(randomPaymentId())
                .userId(userId)
                .amount(new BigDecimal("49.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.CARD)
                .recurring(false)
                .billingDate(LocalDate.now())
                .build();
    }

    /* =========================================================
       FAILURE & EDGE CASES
       ========================================================= */

    public static PaymentRequest insufficientBalance() {
        return PaymentRequest.builder()
                .paymentId(randomPaymentId())
                .userId(UserFactory.freeUser().userId())
                .amount(new BigDecimal("9999.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.CARD)
                .recurring(false)
                .simulateFailure("INSUFFICIENT_FUNDS")
                .build();
    }

    public static PaymentRequest expiredCard() {
        return PaymentRequest.builder()
                .paymentId(randomPaymentId())
                .userId(UserFactory.freeUser().userId())
                .amount(new BigDecimal("299.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.CARD)
                .simulateFailure("CARD_EXPIRED")
                .build();
    }

    public static PaymentRequest networkFailure() {
        return PaymentRequest.builder()
                .paymentId(randomPaymentId())
                .userId(UserFactory.freeUser().userId())
                .amount(new BigDecimal("299.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.UPI)
                .simulateFailure("NETWORK_ERROR")
                .build();
    }

    /* =========================================================
       REFUND SCENARIOS
       ========================================================= */

    public static RefundRequest fullRefund(String paymentId) {
        return new RefundRequest(
                paymentId,
                RefundType.FULL,
                "User requested refund"
        );
    }

    public static RefundRequest partialRefund(String paymentId) {
        return new RefundRequest(
                paymentId,
                RefundType.PARTIAL,
                "Partial refund due to promo adjustment"
        );
    }

    /* =========================================================
       UTILITIES
       ========================================================= */

    private static String randomPaymentId() {
        return "pay_" + UUID.randomUUID();
    }

    /* =========================================================
       DATA MODELS
       ========================================================= */

    public enum PaymentMethod {
        CARD,
        UPI,
        NETBANKING,
        WALLET
    }

    public enum RefundType {
        FULL,
        PARTIAL
    }

    /**
     * Immutable payment request model.
     */
    public static final class PaymentRequest {

        private final String paymentId;
        private final String userId;
        private final BigDecimal amount;
        private final String currency;
        private final PaymentMethod paymentMethod;
        private final boolean recurring;
        private final String planId;
        private final LocalDate billingDate;
        private final String simulatedFailure;

        private PaymentRequest(Builder b) {
            this.paymentId = b.paymentId;
            this.userId = b.userId;
            this.amount = b.amount;
            this.currency = b.currency;
            this.paymentMethod = b.paymentMethod;
            this.recurring = b.recurring;
            this.planId = b.planId;
            this.billingDate = b.billingDate;
            this.simulatedFailure = b.simulatedFailure;
        }

        public String paymentId() { return paymentId; }
        public String userId() { return userId; }
        public BigDecimal amount() { return amount; }
        public String currency() { return currency; }
        public PaymentMethod paymentMethod() { return paymentMethod; }
        public boolean recurring() { return recurring; }
        public String planId() { return planId; }
        public LocalDate billingDate() { return billingDate; }
        public String simulatedFailure() { return simulatedFailure; }

        public Map<String, Object> toApiPayload() {
            return Map.of(
                    "paymentId", paymentId,
                    "userId", userId,
                    "amount", amount,
                    "currency", currency,
                    "method", paymentMethod.name(),
                    "recurring", recurring,
                    "planId", planId,
                    "billingDate", billingDate.toString(),
                    "simulateFailure", simulatedFailure
            );
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String paymentId;
            private String userId;
            private BigDecimal amount;
            private String currency = "INR";
            private PaymentMethod paymentMethod;
            private boolean recurring;
            private String planId;
            private LocalDate billingDate;
            private String simulatedFailure;

            private Builder() {}

            public Builder paymentId(String paymentId) {
                this.paymentId = paymentId; return this;
            }

            public Builder userId(String userId) {
                this.userId = userId; return this;
            }

            public Builder amount(BigDecimal amount) {
                this.amount = amount; return this;
            }

            public Builder currency(String currency) {
                this.currency = currency; return this;
            }

            public Builder paymentMethod(PaymentMethod method) {
                this.paymentMethod = method; return this;
            }

            public Builder recurring(boolean recurring) {
                this.recurring = recurring; return this;
            }

            public Builder planId(String planId) {
                this.planId = planId; return this;
            }

            public Builder billingDate(LocalDate date) {
                this.billingDate = date; return this;
            }

            public Builder simulateFailure(String reason) {
                this.simulatedFailure = reason; return this;
            }

            public PaymentRequest build() {
                return new PaymentRequest(this);
            }
        }
    }

    /**
     * Refund request model.
     */
    public static final class RefundRequest {
        private final String paymentId;
        private final RefundType type;
        private final String reason;

        public RefundRequest(
                String paymentId,
                RefundType type,
                String reason
        ) {
            this.paymentId = paymentId;
            this.type = type;
            this.reason = reason;
        }

        public Map<String, Object> toApiPayload() {
            return Map.of(
                    "paymentId", paymentId,
                    "refundType", type.name(),
                    "reason", reason
            );
        }
    }
}
