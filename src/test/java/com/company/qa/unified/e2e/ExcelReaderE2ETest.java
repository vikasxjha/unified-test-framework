package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.ExcelUtils;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;

/**
 * End-to-End test demonstrating Excel read/write flow.
 *
 * Scenario:
 * 1. Create Excel
 * 2. Write headers
 * 3. Write test data
 * 4. Read & validate
 * 5. Update data
 * 6. Validate update
 */
public class ExcelReaderE2ETest {

    @Test
    public void excelReadWriteE2ETest() throws Exception {

        // 1️⃣ Create temp Excel file
        Path excelPath = Files.createTempFile("automation-test-", ".xlsx");

        try (ExcelUtils excel = new ExcelUtils(excelPath.toString())) {

            // 2️⃣ Create sheet & headers
            excel.writeHeader(
                    "Users",
                    "UserId",
                    "Name",
                    "Role",
                    "Active"
            );

            // 3️⃣ Write data
            excel.writeCell("Users", 1, 0, "U001");
            excel.writeCell("Users", 1, 1, "Vikas");
            excel.writeCell("Users", 1, 2, "Admin");
            excel.writeCell("Users", 1, 3, "true");

            excel.writeCell("Users", 2, 0, "U002");
            excel.writeCell("Users", 2, 1, "Rahul");
            excel.writeCell("Users", 2, 2, "User");
            excel.writeCell("Users", 2, 3, "false");

            excel.save();
        }

        // 4️⃣ Read & validate
        try (ExcelUtils excel = new ExcelUtils(excelPath.toString())) {

            assertEquals(excel.readCell("Users", 1, 1), "Vikas");
            assertEquals(excel.readCell("Users", 2, 2), "User");

            // 5️⃣ Update data
            excel.writeCell("Users", 2, 3, "true");
            excel.save();
        }

        // 6️⃣ Validate updated data
        try (ExcelUtils excel = new ExcelUtils(excelPath.toString())) {

            assertEquals(excel.readCell("Users", 2, 3), "true", "User activation status");
        }

        // Cleanup
        Files.deleteIfExists(excelPath);
    }
}
