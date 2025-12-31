package com.company.qa.unified.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling hyperlinks (<a> tags).
 *
 * Responsibilities:
 * - Count links
 * - Extract link text + URLs
 * - Work on full page or scoped blocks
 */
public final class LinkUtil {

    private LinkUtil() {
        // Utility class
    }

    /**
     * Returns total number of links on the page.
     */
    public static int getLinkCount(Page page) {
        return page.locator("a").count();
    }

    /**
     * Returns all links (text + href) from the page.
     */
    public static List<LinkInfo> getAllLinks(Page page) {
        return extractLinks(page.locator("a"));
    }

    /**
     * Returns all links inside a specific block.
     */
    public static List<LinkInfo> getLinksFromBlock(Page page, String blockSelector) {
        Locator block = page.locator(blockSelector);
        block.waitFor();
        return extractLinks(block.locator("a"));
    }

    /**
     * Internal helper to extract link info.
     */
    private static List<LinkInfo> extractLinks(Locator links) {
        List<LinkInfo> result = new ArrayList<>();
        int count = links.count();

        for (int i = 0; i < count; i++) {
            Locator link = links.nth(i);
            result.add(new LinkInfo(
                    link.innerText().trim(),
                    link.getAttribute("href")
            ));
        }
        return result;
    }

    /**
     * Simple DTO for link information.
     */
    public record LinkInfo(String text, String href) {
    }
}
