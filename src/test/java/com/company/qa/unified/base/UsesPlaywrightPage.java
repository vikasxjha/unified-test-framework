package com.company.qa.unified.base;

import com.microsoft.playwright.Page;

/**
 * Interface to identify test classes that use Playwright Page.
 *
 * This allows the UnifiedTestListener to capture screenshots
 * on test failures.
 *
 * Usage:
 *   public class MyTest extends BaseWebTest implements UsesPlaywrightPage {
 *       // ...
 *   }
 */
public interface UsesPlaywrightPage {

    /**
     * Get the current Playwright Page instance.
     *
     * @return the Page object, or null if not available
     */
    Page getPage();
}

