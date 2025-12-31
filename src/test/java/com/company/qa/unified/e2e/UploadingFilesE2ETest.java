package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.FileUploadUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for file upload functionality.
 *
 * Scenario:
 * - Navigate to upload form
 * - Upload file
 * - Validate file is attached
 */
public class UploadingFilesE2ETest {

    @Test
    public void fileCanBeUploadedSuccessfully() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://www.way2automation.com/way2auto_jquery/registration.php#load_box");
            assertThat(page.title()).contains("Way2Automation");

            String fileInputSelector =
                    "#register_form input[type='file']";

            // 2️⃣ Upload file
            FileUploadUtil.uploadFile(
                    page,
                    fileInputSelector,
                    Paths.get("src/test/resources/files/IMG-6873.jpg")
            );

            // 3️⃣ Validate file attached
            String uploadedFileName =
                    page.locator(fileInputSelector).inputValue();

            assertThat(uploadedFileName)
                    .as("Uploaded file name should be present")
                    .contains("IMG-6873");

            browser.close();
        }
    }
}
