package com.company.qa.unified.utils;

/**
 * Factory for creating MonitoringMail instances.
 * Retrieves SMTP configuration from environment variables.
 */
public final class MailFactory {

    private MailFactory() {}

    /**
     * Creates a MonitoringMail instance configured from environment.
     *
     * Required environment variables:
     * - SMTP_HOST (e.g., smtp.gmail.com)
     * - SMTP_PORT (e.g., 587 for TLS, 465 for SSL)
     * - SMTP_USERNAME
     * - SMTP_PASSWORD
     *
     * @return Configured MonitoringMail instance
     */
    public static MonitoringMail create() {
        String smtpHost = System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com");
        String smtpPort = System.getenv().getOrDefault("SMTP_PORT", "587");
        String username = System.getenv().getOrDefault("SMTP_USERNAME", "");
        String password = System.getenv().getOrDefault("SMTP_PASSWORD", "");

        return new MonitoringMail(smtpHost, smtpPort, username, password);
    }
}

