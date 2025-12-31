package com.company.qa.unified.base;

import com.company.qa.unified.drivers.PlaywrightDriverFactory;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Base class for all Playwright-based web tests.
 *
 * Responsibilities:
 * - Browser lifecycle management
 * - Page instance provision
 * - Screenshot capability via UsesPlaywrightPage interface
 * - Cleanup on test completion
 *
 * Pattern:
 *   All E2E web tests should extend this class.
 */
public abstract class BaseWebTest implements UsesPlaywrightPage {

    protected Playwright playwright;
    protected Page page;

    @BeforeMethod
    public void setUp() {
        page = PlaywrightDriverFactory.getPage();
    }

    @AfterMethod
    public void tearDown() {
        if (page != null) {
            page.close();
        }
        PlaywrightDriverFactory.teardown();
    }

    @Override
    public Page getPage() {
        return page;
    }
}

