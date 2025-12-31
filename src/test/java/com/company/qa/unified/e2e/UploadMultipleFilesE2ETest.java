package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.MultiFileUploadUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for uploading multiple files.
 *
 * Scenario:
 * - Navigate to multi-file upload demo
 * - Upload multiple files inside iframe
 * - Validate uploaded file names
 */
public class UploadMultipleFilesE2ETest {

    @Test
    public void multipleFilesCanBeUploadedSuccessfully() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_fileupload_multiple");
            assertThat(page.title()).contains("Tryit");

            // 2️⃣ Upload multiple files inside iframe
            Path file1 = Paths.get("src/test/resources/files/IMG-6873.jpg");
            Path file2 = Paths.get("src/test/resources/files/IMG-6874.jpg");

            MultiFileUploadUtil.uploadMultipleFilesInFrame(
                    page,
                    "#iframeResult",
                    "#myFile",
                    file1,
                    file2
            );

            // 3️⃣ Validate uploaded file names
            List<String> uploadedFiles =
                    page.frameLocator("#iframeResult")
                            .locator("#myFile")
                            .evaluate("el => Array.from(el.files).map(f => f.name)");

            assertThat(uploadedFiles)
                    .containsExactlyInAnyOrder(
                            "IMG-6873.jpg",
                            "IMG-6874.jpg"
                    );

            browser.close();
        }
    }
}
