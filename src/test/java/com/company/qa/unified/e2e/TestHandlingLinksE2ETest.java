package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.LinkUtil;
import com.company.qa.unified.utils.LinkUtil.LinkInfo;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for handling hyperlinks.
 *
 * Scenario:
 * - Navigate to Wikipedia
 * - Validate total link count
 * - Extract links from a specific page section
 * - Validate link URLs
 */
public class TestHandlingLinksE2ETest {

    @Test
    public void linksCanBeReadAndValidated() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://www.wikipedia.org/");
            assertThat(page.title()).contains("Wikipedia");

            // 2️⃣ Validate total links on page
            int totalLinks = LinkUtil.getLinkCount(page);
            assertThat(totalLinks).isGreaterThan(20);

            // 3️⃣ Extract all page links
            List<LinkInfo> allLinks = LinkUtil.getAllLinks(page);
            assertThat(allLinks).isNotEmpty();

            // Validate that at least one link has valid URL
            assertThat(allLinks)
                    .anyMatch(link -> link.href() != null && link.href().startsWith("http"));

            // 4️⃣ Extract links from specific block (language section)
            List<LinkInfo> blockLinks = LinkUtil.getLinksFromBlock(
                    page,
                    "#www-wikipedia-org"
            );

            assertThat(blockLinks).isNotEmpty();

            // 5️⃣ Validate block links contain expected language
            assertThat(blockLinks)
                    .anyMatch(link -> link.text().contains("English"));

            browser.close();
        }
    }
}
