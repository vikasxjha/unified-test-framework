package com.company.qa.unified.utils;

import com.microsoft.playwright.Page;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * VisualComparator
 *
 * Responsibilities:
 * - Capture screenshots
 * - Compare against baseline
 * - Ignore dynamic regions (ads, timestamps, live banners)
 * - Generate diff images
 *
 * Designed for Playwright-based visual testing.
 */
public class VisualComparator {

    private static final Log log =
            Log.get(VisualComparator.class);

    private final double mismatchThresholdPercent;
    private final Path baselineDir;
    private final Path actualDir;
    private final Path diffDir;

    public VisualComparator(
            Path baselineDir,
            Path actualDir,
            Path diffDir,
            double mismatchThresholdPercent
    ) {
        this.baselineDir = baselineDir;
        this.actualDir = actualDir;
        this.diffDir = diffDir;
        this.mismatchThresholdPercent = mismatchThresholdPercent;
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    public void assertScreenshotMatches(
            Page page,
            String screenshotName,
            List<IgnoreRegion> ignoreRegions
    ) {

        try {
            Files.createDirectories(baselineDir);
            Files.createDirectories(actualDir);
            Files.createDirectories(diffDir);

            Path actualPath =
                    actualDir.resolve(screenshotName + ".png");

            Path baselinePath =
                    baselineDir.resolve(screenshotName + ".png");

            Path diffPath =
                    diffDir.resolve(screenshotName + "-diff.png");

            log.info("📸 Capturing screenshot: {}", screenshotName);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(actualPath)
                    .setFullPage(true));

            if (!Files.exists(baselinePath)) {
                log.warn("⚠️ Baseline missing. Creating new baseline.");
                Files.copy(actualPath, baselinePath);
                return;
            }

            BufferedImage baseline =
                    ImageIO.read(baselinePath.toFile());
            BufferedImage actual =
                    ImageIO.read(actualPath.toFile());

            ComparisonResult result =
                    compareImages(baseline, actual, ignoreRegions);

            ImageIO.write(
                    result.diffImage,
                    "png",
                    diffPath.toFile()
            );

            if (result.mismatchPercent > mismatchThresholdPercent) {
                throw new AssertionError(
                        String.format(
                                "Visual mismatch %.2f%% exceeds threshold %.2f%%. Diff: %s",
                                result.mismatchPercent,
                                mismatchThresholdPercent,
                                diffPath
                        )
                );
            }

            log.info(
                    "✅ Visual match OK (mismatch=%.2f%%)",
                    result.mismatchPercent
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Visual comparison failed", e);
        }
    }

    /* =========================================================
       IMAGE COMPARISON CORE
       ========================================================= */

    private ComparisonResult compareImages(
            BufferedImage expected,
            BufferedImage actual,
            List<IgnoreRegion> ignoreRegions
    ) {

        int width =
                Math.min(expected.getWidth(), actual.getWidth());
        int height =
                Math.min(expected.getHeight(), actual.getHeight());

        BufferedImage diff =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int totalPixels = width * height;
        int mismatchedPixels = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (isIgnored(x, y, ignoreRegions)) {
                    diff.setRGB(x, y, expected.getRGB(x, y));
                    continue;
                }

                int rgb1 = expected.getRGB(x, y);
                int rgb2 = actual.getRGB(x, y);

                if (rgb1 != rgb2) {
                    diff.setRGB(x, y, Color.RED.getRGB());
                    mismatchedPixels++;
                } else {
                    diff.setRGB(x, y, rgb1);
                }
            }
        }

        double mismatchPercent =
                (mismatchedPixels * 100.0) / totalPixels;

        return new ComparisonResult(diff, mismatchPercent);
    }

    private boolean isIgnored(
            int x,
            int y,
            List<IgnoreRegion> regions
    ) {
        for (IgnoreRegion r : regions) {
            if (r.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    /* =========================================================
       SUPPORTING TYPES
       ========================================================= */

    private static class ComparisonResult {
        final BufferedImage diffImage;
        final double mismatchPercent;

        ComparisonResult(
                BufferedImage diffImage,
                double mismatchPercent
        ) {
            this.diffImage = diffImage;
            this.mismatchPercent = mismatchPercent;
        }
    }

    /* =========================================================
       IGNORE REGION MODEL
       ========================================================= */

    public static class IgnoreRegion {

        private final Rectangle rect;

        public IgnoreRegion(int x, int y, int width, int height) {
            this.rect = new Rectangle(x, y, width, height);
        }

        public boolean contains(int x, int y) {
            return rect.contains(x, y);
        }

        /* --------- Builders --------- */

        public static IgnoreRegion of(
                int x, int y, int width, int height
        ) {
            return new IgnoreRegion(x, y, width, height);
        }
    }

    /* =========================================================
       COMMON IGNORE REGIONS
       ========================================================= */

    public static List<IgnoreRegion> defaultDynamicRegions() {

        List<IgnoreRegion> regions = new ArrayList<>();

        // Top banner ads
        regions.add(IgnoreRegion.of(0, 0, 1200, 200));

        // Footer timestamps
        regions.add(IgnoreRegion.of(0, 1800, 1200, 200));

        // Right-side live widgets
        regions.add(IgnoreRegion.of(900, 200, 300, 1400));

        return regions;
    }
}
