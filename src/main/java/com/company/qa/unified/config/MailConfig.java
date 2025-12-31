package com.company.qa.unified.config;

/**
 * Centralized mail configuration.
 * Values must come from env variables or CI secrets.
 */
public final class MailConfig {

    private MailConfig() {}

    public static final String SMTP_HOST =
            System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com");

    public static final String SMTP_PORT =
            System.getenv().getOrDefault("SMTP_PORT", "465");

    public static final String SMTP_USER =
            System.getenv("SMTP_USER");

    public static final String SMTP_PASSWORD =
            System.getenv("SMTP_PASSWORD");

    public static final String FROM_EMAIL =
            System.getenv().getOrDefault("MAIL_FROM", SMTP_USER);
}
