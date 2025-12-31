package com.company.qa.unified.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;

public class ExcelUtils implements Closeable {

    private final String filePath;
    private final Workbook workbook;

    public ExcelUtils(String filePath) {
        this.filePath = filePath;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                workbook = new XSSFWorkbook(new FileInputStream(file));
            } else {
                workbook = new XSSFWorkbook();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Excel file", e);
        }
    }

    /* =============================
       Sheet Operations
       ============================= */

    public Sheet getOrCreateSheet(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        return sheet != null ? sheet : workbook.createSheet(sheetName);
    }

    public int getRowCount(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        return sheet == null ? 0 : sheet.getLastRowNum() + 1;
    }

    /* =============================
       Read Operations
       ============================= */

    public String readCell(String sheetName, int rowNum, int colNum) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) return "";

        Row row = sheet.getRow(rowNum);
        if (row == null) return "";

        Cell cell = row.getCell(colNum);
        if (cell == null) return "";

        return getCellValueAsString(cell);
    }

    /* =============================
       Write Operations
       ============================= */

    public void writeCell(String sheetName, int rowNum, int colNum, String value) {
        Sheet sheet = getOrCreateSheet(sheetName);
        Row row = sheet.getRow(rowNum);
        if (row == null) row = sheet.createRow(rowNum);

        Cell cell = row.getCell(colNum);
        if (cell == null) cell = row.createCell(colNum);

        cell.setCellValue(value);
    }

    public void writeHeader(String sheetName, String... headers) {
        Sheet sheet = getOrCreateSheet(sheetName);
        Row headerRow = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    /* =============================
       Save & Close
       ============================= */

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save Excel file", e);
        }
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }

    /* =============================
       Helpers
       ============================= */

    private String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? LocalDate.ofInstant(cell.getDateCellValue().toInstant(),
                    java.time.ZoneId.systemDefault()).toString()
                    : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
