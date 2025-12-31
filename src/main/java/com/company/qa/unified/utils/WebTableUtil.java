package com.company.qa.unified.utils;

import com.microsoft.playwright.Locator;

import java.util.List;

/**
 * Utility class for interacting with HTML tables.
 *
 * Responsibilities:
 * - Row & column counting
 * - Cell value retrieval
 * - Row search by cell value
 * - Table data extraction
 */
public final class WebTableUtil {

    private WebTableUtil() {
        // Utility class
    }

    /**
     * Returns number of rows in table body.
     */
    public static int getRowCount(Locator tableBody) {
        return tableBody.locator("tr").count();
    }

    /**
     * Returns number of columns in a given row.
     */
    public static int getColumnCount(Locator row) {
        return row.locator("td").count();
    }

    /**
     * Returns text of a specific cell (1-based index).
     */
    public static String getCellText(Locator tableBody, int rowIndex, int colIndex) {
        return tableBody
                .locator("tr").nth(rowIndex - 1)
                .locator("td").nth(colIndex - 1)
                .innerText()
                .trim();
    }

    /**
     * Returns all rows text from table body.
     */
    public static List<String> getAllRowsText(Locator tableBody) {
        return tableBody.allInnerTexts();
    }

    /**
     * Finds a row index by matching text in a specific column.
     * Returns -1 if not found.
     */
    public static int findRowByCellText(Locator tableBody, int colIndex, String expectedText) {
        int rows = getRowCount(tableBody);
        for (int i = 1; i <= rows; i++) {
            String cellText = getCellText(tableBody, i, colIndex);
            if (cellText.equalsIgnoreCase(expectedText)) {
                return i;
            }
        }
        return -1;
    }
}
