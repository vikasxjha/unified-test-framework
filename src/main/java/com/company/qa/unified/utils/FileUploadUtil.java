package com.company.qa.unified.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;

/**
 * Utility class for handling file uploads in Playwright.
 *
 * Responsibilities:
 * - Upload single or multiple files
 * - Ensure input is ready before upload
 * - Keep upload logic out of test cases
 */
public final class FileUploadUtil {

    private FileUploadUtil() {
        // Utility class
    }

    /**
     * Uploads a single file.
     *
     * @param page Playwright page
     * @param fileInputSelector Selector for <input type="file">
     * @param filePath Path to file
     */
    public static void uploadFile(
            Page page,
            String fileInputSelector,
            Path filePath
    ) {
        Locator fileInput = page.locator(fileInputSelector);
        fileInput.waitFor();
        fileInput.setInputFiles(filePath);
    }

    /**
     * Uploads multiple files.
     */
    public static void uploadMultipleFiles(
            Page page,
            String fileInputSelector,
            Path... filePaths
    ) {
        Locator fileInput = page.locator(fileInputSelector);
        fileInput.waitFor();
        fileInput.setInputFiles(filePaths);
    }
}
