package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.WebTableUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for handling web tables using Playwright.
 *
 * Scenario:
 * - Navigate to NIFTY 50 index table
 * - Validate rows & columns
 * - Validate specific cell value
 * - Search table data
 */
public class HandlingWebTablesE2ETest {

    @Test
    public void webTableDataCanBeReadAndValidated() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://money.rediff.com/indices/nse/NIFTY-50?src=moneyhome_nseIndices");
            assertThat(page.title()).contains("NIFTY");

            // 2️⃣ Locate table body
            Locator tableBody = page.locator(".dataTable > tbody");

            // 3️⃣ Validate row count
            int rowCount = WebTableUtil.getRowCount(tableBody);
            assertThat(rowCount).isGreaterThan(0);

            // 4️⃣ Validate column count (from first row)
            int columnCount = WebTableUtil.getColumnCount(
                    tableBody.locator("tr").first()
            );
            assertThat(columnCount).isGreaterThan(3);

            // 5️⃣ Validate specific cell value (Row 1, Column 2)
            String indexName = WebTableUtil.getCellText(tableBody, 1, 2);
            assertThat(indexName).isEqualTo("Nifty");

            // 6️⃣ Find row containing "Nifty"
            int niftyRow = WebTableUtil.findRowByCellText(tableBody, 2, "Nifty");
            assertThat(niftyRow).isGreaterThan(0);

            // 7️⃣ Print table rows (debug visibility)
            WebTableUtil.getAllRowsText(tableBody)
                    .forEach(row -> System.out.println("ROW ➜ " + row));

            browser.close();
        }
    }
}
