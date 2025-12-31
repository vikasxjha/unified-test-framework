package com.company.qa.unified.utils;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for handling HTML <select> dropdowns.
 *
 * Responsibilities:
 * - Select options by value, label, index
 * - Fetch available options
 * - Keep dropdown logic out of tests
 */
public final class DropdownUtil {

    private DropdownUtil() {
        // Utility class
    }

    /**
     * Selects dropdown option by value.
     */
    public static void selectByValue(Page page, String dropdownSelector, String value) {
        page.locator(dropdownSelector).waitFor();
        page.selectOption(dropdownSelector, value);
    }

    /**
     * Selects dropdown option by visible label.
     */
    public static void selectByLabel(Page page, String dropdownSelector, String label) {
        page.locator(dropdownSelector).waitFor();
        page.selectOption(dropdownSelector, new SelectOption().setLabel(label));
    }

    /**
     * Selects dropdown option by index.
     */
    public static void selectByIndex(Page page, String dropdownSelector, int index) {
        page.locator(dropdownSelector).waitFor();
        page.selectOption(dropdownSelector, new SelectOption().setIndex(index));
    }

    /**
     * Returns all option texts from dropdown.
     */
    public static List<String> getAllOptionTexts(Page page, String dropdownSelector) {
        List<ElementHandle> options =
                page.querySelectorAll(dropdownSelector + " > option");

        return options.stream()
                .map(ElementHandle::innerText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Returns total number of options in dropdown.
     */
    public static int getOptionCount(Page page, String dropdownSelector) {
        return page.locator(dropdownSelector + " > option").count();
    }
}
