package com.company.qa.unified.e2e;

import com.company.qa.unified.base.BaseWebTest;
import com.company.qa.unified.utils.DownloadUtil;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for file download using Playwright.
 *
 * Scenario:
 * - Navigate to Selenium downloads page
 * - Download selenium-server jar
 * - Validate file exists and is non-empty
 */
public class DownloadingFilesE2ETest extends BaseWebTest {

    @Test
    public void seleniumJarIsDownloadedSuccessfully() throws Exception {

        // 1️⃣ Navigate
        page.navigate("https://www.selenium.dev/downloads/");
        assertThat(page.title()).contains("Selenium");

        // 2️⃣ Trigger download
        Path downloadedFile = DownloadUtil.downloadFile(
                page,
                () -> page.locator("a[href*='selenium-server']").first().click(),
                "src/test/resources/files",
                "selenium-server.jar"
        );

        // 3️⃣ Validate file
        DownloadUtil.validateDownload(downloadedFile);

        // 4️⃣ Additional validation (optional)
        assertThat(Files.size(downloadedFile))
                .as("Downloaded file size")
                .isGreaterThan(1_000_000); // > 1MB
    }
}
