package com.company.qa.unified.utils;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to handle file downloads in Playwright.
 *
 * Responsibilities:
 * - Wait for downloads
 * - Persist files to disk
 * - Validate downloaded artifacts
 */
public final class DownloadUtil {

    private DownloadUtil() {
        // utility class
    }

    /**
     * Triggers a download action and saves the file to target directory.
     *
     * @param page Playwright page
     * @param downloadTrigger Action that initiates the download
     * @param targetDir Directory to save the file
     * @param fileName File name to save as
     * @return Path of downloaded file
     */
    public static Path downloadFile(
            Page page,
            Runnable downloadTrigger,
            String targetDir,
            String fileName
    ) {

        Download download = page.waitForDownload(downloadTrigger);

        try {
            Path directory = Paths.get(targetDir);
            Files.createDirectories(directory);

            Path filePath = directory.resolve(fileName);
            download.saveAs(filePath);

            return filePath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    /**
     * Validates downloaded file existence and size.
     */
    public static void validateDownload(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                throw new AssertionError("Downloaded file does not exist: " + filePath);
            }
            if (Files.size(filePath) == 0) {
                throw new AssertionError("Downloaded file is empty: " + filePath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate downloaded file", e);
        }
    }
}
