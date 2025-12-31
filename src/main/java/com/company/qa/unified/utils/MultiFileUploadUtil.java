package com.company.qa.unified.utils;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;

/**
 * Utility class for handling MULTIPLE file uploads.
 *
 * Responsibilities:
 * - Upload multiple files
 * - Support iframe-based file inputs
 * - Keep upload logic out of tests
 */
public final class MultiFileUploadUtil {

    private MultiFileUploadUtil() {
        // Utility class
    }

    /**
     * Uploads multiple files to a file input on the main page.
     */
    public static void uploadMultipleFiles(
            Page page,
            String fileInputSelector,
            Path... files
    ) {
        Locator fileInput = page.locator(fileInputSelector);
        fileInput.waitFor();
        fileInput.setInputFiles(files);
    }

    /**
     * Uploads multiple files to a file input inside an iframe.
     */
    public static void uploadMultipleFilesInFrame(
            Page page,
            String frameSelector,
            String fileInputSelector,
            Path... files
    ) {
        FrameLocator frame = page.frameLocator(frameSelector);
        Locator fileInput = frame.locator(fileInputSelector);
        fileInput.waitFor();
        fileInput.setInputFiles(files);
    }
}
