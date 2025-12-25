package com.company.qa.unified.pages.web;

import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WebActions;
import com.microsoft.playwright.Page;

/**
 * BaseWebPage
 *
 * Base class for all web page objects.
 *
 * Provides:
 * - Common page functionality
 * - WebActions wrapper
 * - Logging
 * - Waits
 */
public abstract class BaseWebPage {

    protected final Log log = Log.get(this.getClass());
    protected final Page page;
    protected final WebActions actions;

    protected BaseWebPage(Page page) {
        this.page = page;
        this.actions = new WebActions(page);
    }

    /**
     * Get current page URL.
     */
    public String getCurrentUrl() {
        return page.url();
    }

    /**
     * Get page title.
     */
    public String getTitle() {
        return page.title();
    }

    /**
     * Wait for page to be fully loaded.
     */
    public void waitForPageLoad() {
        actions.waitForDomReady();
    }

    /**
     * Take screenshot.
     */
    public byte[] takeScreenshot() {
        return page.screenshot();
    }

    /**
     * Refresh page.
     */
    public void refresh() {
        page.reload();
    }

    /**
     * Navigate back.
     */
    public void goBack() {
        page.goBack();
    }

    /**
     * Abstract method for page-specific validation.
     */
    public abstract void assertPageLoaded();
}

