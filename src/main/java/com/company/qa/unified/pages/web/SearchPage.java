package com.company.qa.unified.pages.web;

import com.microsoft.playwright.Page;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchPage
 *
 * Page Object for search results page.
 *
 * Covers:
 * - Search results display
 * - Filtering
 * - Sorting
 * - Pagination
 */
public class SearchPage extends BaseWebPage {

    // Selectors
    private static final String SEARCH_RESULTS = "[data-testid='search-results']";
    private static final String RESULT_ITEM = "[data-testid='result-item']";
    private static final String NO_RESULTS_MESSAGE = "[data-testid='no-results']";
    private static final String FILTER_BUTTON = "[data-testid='filter-button']";
    private static final String SORT_DROPDOWN = "[data-testid='sort-dropdown']";
    private static final String PAGINATION = "[data-testid='pagination']";
    private static final String NEXT_PAGE_BUTTON = "[data-testid='next-page']";
    private static final String PREV_PAGE_BUTTON = "[data-testid='prev-page']";
    private static final String RESULTS_COUNT = "[data-testid='results-count']";

    public SearchPage(Page page) {
        super(page);
    }

    /* =========================================================
       ASSERTIONS
       ========================================================= */

    @Override
    public void assertPageLoaded() {
        log.info("✅ Verifying search page loaded");

        actions.waitForVisible(SEARCH_RESULTS, 10);
        assertTrue(actions.isVisible(SEARCH_RESULTS), "Search results container not visible");

        log.info("Search page loaded successfully");
    }

    public void assertResultsDisplayed() {
        log.info("✅ Verifying search results displayed");

        actions.waitForVisible(RESULT_ITEM, 10);
        assertTrue(actions.isVisible(RESULT_ITEM), "No search results displayed");

        int count = getResultCount();
        assertTrue(count > 0, "No search results found");

        log.info("Found {} search results", count);
    }

    public void assertNoResults() {
        log.info("✅ Verifying no results message");

        actions.waitForVisible(NO_RESULTS_MESSAGE, 10);
        assertTrue(actions.isVisible(NO_RESULTS_MESSAGE), "No results message not displayed");
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    public SearchPage applyFilter(String filterName) {
        log.info("🔽 Applying filter: {}", filterName);

        actions.click(FILTER_BUTTON);
        actions.click(String.format("[data-filter='%s']", filterName));
        actions.waitForNetworkIdle(3);

        return this;
    }

    public SearchPage sortBy(String sortOption) {
        log.info("🔽 Sorting by: {}", sortOption);

        actions.click(SORT_DROPDOWN);
        actions.click(String.format("[data-sort='%s']", sortOption));
        actions.waitForNetworkIdle(3);

        return this;
    }

    public SearchPage goToNextPage() {
        log.info("➡️ Going to next page");

        actions.click(NEXT_PAGE_BUTTON);
        actions.waitForNetworkIdle(3);

        return this;
    }

    public SearchPage goToPreviousPage() {
        log.info("⬅️ Going to previous page");

        actions.click(PREV_PAGE_BUTTON);
        actions.waitForNetworkIdle(3);

        return this;
    }

    public void clickResult(int index) {
        log.info("👆 Clicking result at index: {}", index);

        String selector = String.format("(%s)[%d]", RESULT_ITEM, index + 1);
        actions.click(selector);
    }

    /* =========================================================
       GETTERS
       ========================================================= */

    public int getResultCount() {
        return page.locator(RESULT_ITEM).count();
    }

    public String getResultsCountText() {
        if (actions.isVisible(RESULTS_COUNT)) {
            return actions.getText(RESULTS_COUNT);
        }
        return String.valueOf(getResultCount());
    }

    public boolean hasResults() {
        return getResultCount() > 0;
    }

    public boolean hasNextPage() {
        return actions.isVisible(NEXT_PAGE_BUTTON) && actions.isEnabled(NEXT_PAGE_BUTTON);
    }

    public boolean hasPreviousPage() {
        return actions.isVisible(PREV_PAGE_BUTTON) && actions.isEnabled(PREV_PAGE_BUTTON);
    }
}

