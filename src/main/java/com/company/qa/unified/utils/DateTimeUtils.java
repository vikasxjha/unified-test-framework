package com.company.qa.unified.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * DateTimeUtils
 *
 * Central date & time utility for:
 * - API timestamps
 * - Event validation
 * - DB assertions
 * - UI time comparisons
 *
 * Design goals:
 * - Timezone-safe
 * - Test-friendly
 * - Deterministic when needed
 */
public final class DateTimeUtils {

    private static final Log log =
            Log.get(DateTimeUtils.class);

    private DateTimeUtils() {
        // utility class
    }

    /* =========================================================
       COMMON FORMATTERS
       ========================================================= */

    public static final DateTimeFormatter ISO_UTC =
            DateTimeFormatter.ISO_INSTANT;

    public static final DateTimeFormatter ISO_OFFSET =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static final DateTimeFormatter ISO_LOCAL =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static final DateTimeFormatter DATE_ONLY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DATE_TIME_UI =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    /* =========================================================
       NOW / CURRENT TIME
       ========================================================= */

    /**
     * Current UTC Instant.
     */
    public static Instant nowUtc() {
        return Instant.now();
    }

    /**
     * Current time in given zone.
     */
    public static ZonedDateTime nowInZone(ZoneId zone) {
        return ZonedDateTime.now(zone);
    }

    /**
     * Current local date.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /* =========================================================
       FORMATTERS
       ========================================================= */

    public static String formatInstant(Instant instant) {
        return ISO_UTC.format(instant);
    }

    public static String formatInstant(
            Instant instant,
            DateTimeFormatter formatter
    ) {
        return formatter.withZone(ZoneOffset.UTC).format(instant);
    }

    public static String formatZoned(
            ZonedDateTime time,
            DateTimeFormatter formatter
    ) {
        return formatter.format(time);
    }

    /* =========================================================
       PARSING
       ========================================================= */

    public static Instant parseInstant(String isoInstant) {
        return Instant.parse(isoInstant);
    }

    public static ZonedDateTime parseZoned(
            String value,
            DateTimeFormatter formatter,
            ZoneId zone
    ) {
        return LocalDateTime.parse(value, formatter).atZone(zone);
    }

    public static LocalDate parseDate(
            String date,
            DateTimeFormatter formatter
    ) {
        return LocalDate.parse(date, formatter);
    }

    /* =========================================================
       ARITHMETIC
       ========================================================= */

    public static Instant addSeconds(Instant instant, long seconds) {
        return instant.plusSeconds(seconds);
    }

    public static Instant addMinutes(Instant instant, long minutes) {
        return instant.plus(minutes, ChronoUnit.MINUTES);
    }

    public static Instant addHours(Instant instant, long hours) {
        return instant.plus(hours, ChronoUnit.HOURS);
    }

    public static Instant addDays(Instant instant, long days) {
        return instant.plus(days, ChronoUnit.DAYS);
    }

    public static LocalDate addDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    /* =========================================================
       COMPARISONS
       ========================================================= */

    public static boolean isBefore(Instant a, Instant b) {
        return a.isBefore(b);
    }

    public static boolean isAfter(Instant a, Instant b) {
        return a.isAfter(b);
    }

    /**
     * Assert two instants are within tolerance.
     */
    public static boolean withinTolerance(
            Instant expected,
            Instant actual,
            Duration tolerance
    ) {

        Duration diff =
                Duration.between(expected, actual).abs();

        boolean result = diff.compareTo(tolerance) <= 0;

        log.debug(
                "Time diff={}ms tolerance={}ms result={}",
                diff.toMillis(),
                tolerance.toMillis(),
                result
        );

        return result;
    }

    /* =========================================================
       EPOCH HELPERS
       ========================================================= */

    public static long toEpochMillis(Instant instant) {
        return instant.toEpochMilli();
    }

    public static Instant fromEpochMillis(long millis) {
        return Instant.ofEpochMilli(millis);
    }

    /* =========================================================
       RANGE / WINDOW VALIDATION
       ========================================================= */

    /**
     * Check if instant lies between start and end (inclusive).
     */
    public static boolean isBetween(
            Instant target,
            Instant start,
            Instant end
    ) {
        return !target.isBefore(start) && !target.isAfter(end);
    }

    /* =========================================================
       TEST HELPERS
       ========================================================= */

    /**
     * Fixed instant for deterministic tests.
     */
    public static Instant fixedInstant() {
        return Instant.parse("2025-01-01T00:00:00Z");
    }

    /**
     * Sleep helper (ONLY for infra waits, not UI).
     */
    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
