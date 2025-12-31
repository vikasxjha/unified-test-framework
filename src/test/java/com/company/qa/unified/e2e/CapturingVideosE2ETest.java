package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.VideoCaptureUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test demonstrating:
 * - Playwright video recording
 * - UI interaction
 * - Validation
 * - Artifact generation
 */
public class CapturingVideosE2ETest {

    @Test
    public void wikipediaLanguageSelectionIsRecorded() throws Exception {

        try (Playwright playwright = Playwright.create()) {

            // 1️⃣ Create context with video recording
            BrowserContext context =
                    VideoCaptureUtil.createContextWithVideo(
                            playwright,
                            playwright.chromium(),
                            false,
                            "videos"
                    );

            Page page = context.newPage();

            // 2️⃣ Navigate
            page.navigate("https://www.wikipedia.org/");
            assertThat(page.title()).contains("Wikipedia");

            // 3️⃣ Select language by value
            page.selectOption("select", "hi");

            // 4️⃣ Select language by visible text
            page.selectOption("select", new SelectOption().setLabel("Eesti"));

            // 5️⃣ Select language by index
            page.selectOption("select", new SelectOption().setIndex(1));

            // 6️⃣ Validate dropdown options exist
            int optionCount = page.querySelectorAll("select > option").size();
            assertThat(optionCount).isGreaterThan(50);

            // 7️⃣ Close page & context (IMPORTANT for video saving)
            page.close();
            context.close();

            // 8️⃣ Validate video file exists
            Path videoDir = Path.of("videos");
            assertThat(Files.exists(videoDir)).isTrue();
            assertThat(Files.list(videoDir).findAny()).isPresent();
        }
    }
}
