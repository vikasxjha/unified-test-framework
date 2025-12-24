package com.company.qa.unified.data;

import com.company.qa.unified.config.EnvironmentConfig;

import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating user-related test data.
 *
 * IMPORTANT:
 * - This class ONLY creates user objects
 * - It does NOT provision users in DB / API
 * - Provisioning is handled by TestDataProvisioner
 *
 * Design goals:
 * - Deterministic roles
 * - Clear intent (free / premium / admin)
 * - Safe for non-prod usage
 */
public final class UserFactory {

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    private UserFactory() {
        // utility
    }

    /* =========================================================
       USER CREATION – COMMON ROLES
       ========================================================= */

    public static User freeUser() {
        return baseUser(UserType.FREE);
    }

    public static User premiumUser() {
        return baseUser(UserType.PREMIUM);
    }

    public static User adminUser() {
        return baseUser(UserType.ADMIN);
    }

    /* =========================================================
       USER CREATION – VARIANTS
       ========================================================= */

    public static User suspendedUser() {
        return User.builder()
                .userId(randomUserId())
                .phone("+919000000001")
                .email("suspended.user@test.com")
                .userType(UserType.FREE)
                .status(UserStatus.SUSPENDED)
                .countryCode("IN")
                .build();
    }

    public static User unverifiedUser() {
        return User.builder()
                .userId(randomUserId())
                .phone("+919000000002")
                .email("unverified.user@test.com")
                .userType(UserType.FREE)
                .status(UserStatus.UNVERIFIED)
                .countryCode("IN")
                .build();
    }

    /* =========================================================
       BASE USER BUILDER
       ========================================================= */

    private static User baseUser(UserType type) {

        String prefix = switch (type) {
            case FREE -> "free";
            case PREMIUM -> "premium";
            case ADMIN -> "admin";
        };

        return User.builder()
                .userId(randomUserId())
                .phone(generatePhone(type))
                .email(prefix + ".user@test.com")
                .userType(type)
                .status(UserStatus.ACTIVE)
                .countryCode("IN")
                .locale("en_IN")
                .build();
    }

    /* =========================================================
       UTILITIES
       ========================================================= */

    private static String randomUserId() {
        return "user_" + UUID.randomUUID();
    }

    private static String generatePhone(UserType type) {
        return switch (type) {
            case FREE -> "+91911111" + random4();
            case PREMIUM -> "+91922222" + random4();
            case ADMIN -> "+91933333" + random4();
        };
    }

    private static String random4() {
        return String.format("%04d", (int) (Math.random() * 10000));
    }

    /* =========================================================
       USER MODEL
       ========================================================= */

    public enum UserType {
        FREE,
        PREMIUM,
        ADMIN
    }

    public enum UserStatus {
        ACTIVE,
        SUSPENDED,
        UNVERIFIED,
        DELETED
    }

    /**
     * Immutable user model.
     */
    public static final class User {

        private final String userId;
        private final String phone;
        private final String email;
        private final UserType userType;
        private final UserStatus status;
        private final String countryCode;
        private final String locale;

        private User(Builder b) {
            this.userId = b.userId;
            this.phone = b.phone;
            this.email = b.email;
            this.userType = b.userType;
            this.status = b.status;
            this.countryCode = b.countryCode;
            this.locale = b.locale;
        }

        public String userId() { return userId; }
        public String phone() { return phone; }
        public String email() { return email; }
        public UserType userType() { return userType; }
        public UserStatus status() { return status; }
        public String countryCode() { return countryCode; }
        public String locale() { return locale; }

        /**
         * Convert to API payload.
         */
        public Map<String, Object> toApiPayload() {
            return Map.of(
                    "userId", userId,
                    "phone", phone,
                    "email", email,
                    "type", userType.name(),
                    "status", status.name(),
                    "countryCode", countryCode,
                    "locale", locale
            );
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public String toString() {
            return """
                   User {
                     userId='%s'
                     phone='%s'
                     email='%s'
                     type=%s
                     status=%s
                   }
                   """.formatted(
                    userId, phone, email, userType, status
            );
        }

        public static final class Builder {
            private String userId;
            private String phone;
            private String email;
            private UserType userType;
            private UserStatus status;
            private String countryCode;
            private String locale;

            private Builder() {}

            public Builder userId(String userId) {
                this.userId = userId; return this;
            }

            public Builder phone(String phone) {
                this.phone = phone; return this;
            }

            public Builder email(String email) {
                this.email = email; return this;
            }

            public Builder userType(UserType userType) {
                this.userType = userType; return this;
            }

            public Builder status(UserStatus status) {
                this.status = status; return this;
            }

            public Builder countryCode(String countryCode) {
                this.countryCode = countryCode; return this;
            }

            public Builder locale(String locale) {
                this.locale = locale; return this;
            }

            public User build() {
                return new User(this);
            }
        }
    }
}
