package com.company.qa.unified.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * CsvUtils
 *
 * Utility class for CSV handling used in:
 * - Data-driven tests
 * - DB export validation
 * - API bulk testing
 * - Performance / analytics assertions
 *
 * Design goals:
 * - No external dependencies
 * - Header-aware
 * - CI-safe
 * - Predictable parsing
 */
public final class CsvUtils {

    private static final Log log =
            Log.get(CsvUtils.class);

    private CsvUtils() {
        // utility class
    }

    /* =========================================================
       READ CSV
       ========================================================= */

    /**
     * Read CSV as List of String arrays (raw rows).
     */
    public static List<String[]> readCsv(Path path) {

        log.info("📄 Reading CSV file {}", path);

        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(parseLine(line));
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read CSV file: " + path, e);
        }

        return rows;
    }

    /**
     * Read CSV as List of Maps (header → value).
     */
    public static List<Map<String, String>> readCsvAsMaps(Path path) {

        log.info("📄 Reading CSV as map {}", path);

        List<String[]> rows = readCsv(path);

        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        String[] headers = rows.get(0);
        List<Map<String, String>> result = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] values = rows.get(i);
            Map<String, String> row = new LinkedHashMap<>();

            for (int j = 0; j < headers.length; j++) {
                String key = headers[j];
                String value = j < values.length ? values[j] : "";
                row.put(key, value);
            }
            result.add(row);
        }

        return result;
    }

    /* =========================================================
       WRITE CSV
       ========================================================= */

    /**
     * Write CSV from headers + rows.
     */
    public static void writeCsv(
            Path path,
            List<String> headers,
            List<List<String>> rows
    ) {

        log.info("📝 Writing CSV file {}", path);

        try (BufferedWriter writer =
                     Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {

            writer.write(String.join(",", escapeAll(headers)));
            writer.newLine();

            for (List<String> row : rows) {
                writer.write(String.join(",", escapeAll(row)));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to write CSV file: " + path, e);
        }
    }

    /**
     * Write CSV from list of maps (headers inferred).
     */
    public static void writeCsvFromMaps(
            Path path,
            List<Map<String, String>> rows
    ) {

        if (rows.isEmpty()) {
            log.warn("CSV write skipped: no rows");
            return;
        }

        List<String> headers =
                new ArrayList<>(rows.get(0).keySet());

        List<List<String>> values = new ArrayList<>();

        for (Map<String, String> row : rows) {
            List<String> line = new ArrayList<>();
            for (String h : headers) {
                line.add(row.getOrDefault(h, ""));
            }
            values.add(line);
        }

        writeCsv(path, headers, values);
    }

    /* =========================================================
       LOOKUP HELPERS
       ========================================================= */

    /**
     * Find first row where column equals value.
     */
    public static Optional<Map<String, String>> findRow(
            List<Map<String, String>> rows,
            String column,
            String expectedValue
    ) {

        return rows.stream()
                .filter(r ->
                        expectedValue.equals(r.get(column)))
                .findFirst();
    }

    /* =========================================================
       INTERNAL PARSING
       ========================================================= */

    /**
     * Basic CSV parser supporting quoted values.
     */
    private static String[] parseLine(String line) {

        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private static List<String> escapeAll(List<String> values) {
        List<String> escaped = new ArrayList<>();
        for (String v : values) {
            escaped.add(escape(v));
        }
        return escaped;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
