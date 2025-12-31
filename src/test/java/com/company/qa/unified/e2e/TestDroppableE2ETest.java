package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.DragAndDropUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for drag-and-drop functionality.
 *
 * Scenario:
 * - Navigate to droppable demo page
 * - Drag draggable element
 * - Drop onto droppable target
 * - Validate successful drop
 */
public class TestDroppableE2ETest {

    @Test
    public void draggableElementCanBeDroppedSuccessfully() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://jqueryui.com/resources/demos/droppable/default.html");
            assertThat(page.title()).contains("Droppable");

            // 2️⃣ Locate elements
            Locator draggable = page.locator("#draggable");
            Locator droppable = page.locator("#droppable");

            // 3️⃣ Perform drag and drop
            DragAndDropUtil.dragAndDrop(page, draggable, droppable);

            // 4️⃣ Validate drop result
            String dropText = droppable.textContent();
            assertThat(dropText)
                    .as("Droppable success text")
                    .containsIgnoringCase("Dropped");

            browser.close();
        }
    }
}
