package org.example.buttonforload.service;

import org.apache.poi.ss.usermodel.*;
import org.example.buttonforload.dto.ResourceRowDto;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class XlsxParseService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public List<ResourceRowDto> parse(Path filePath) {
        List<ResourceRowDto> rows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream inputStream = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            int sheetCount = workbook.getNumberOfSheets();
            System.out.println("Workbook sheets count = " + sheetCount);

            if (sheetCount == 0) {
                throw new IllegalStateException("В книге нет листов: " + filePath);
            }

            for (int s = 0; s < sheetCount; s++) {
                System.out.println("Sheet " + s + " = " + workbook.getSheetName(s));
            }

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String c0 = cellValue(formatter, row.getCell(0));
                String c1 = cellValue(formatter, row.getCell(1));
                String c2 = cellValue(formatter, row.getCell(2));
                String c3 = cellValue(formatter, row.getCell(3));
                String c4 = cellValue(formatter, row.getCell(4));

                if (isAllBlank(c0, c1, c2, c3, c4)) {
                    continue;
                }

                ResourceRowDto dto = new ResourceRowDto();
                dto.setNumber(parseIntegerSafe(c0));
                dto.setFullName(blankToNull(c1));
                dto.setInclusionGrounds(blankToNull(c2));
                dto.setInclusionDecisionDate(parseDateSafe(c3));
                dto.setExclusionDecisionDate(parseDateSafe(c4));

                rows.add(dto);
            }

            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось прочитать XLSX файл: " + filePath, e);
        }
    }

    private String cellValue(DataFormatter formatter, Cell cell) {
        if (cell == null) {
            return null;
        }
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isAllBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private Integer parseIntegerSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.replaceAll("\\s+", ""));
        } catch (NumberFormatException e) {
            System.out.println("Skip number parse, value='" + value + "'");
            return null;
        }
    }

    private LocalDate parseDateSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String v = value.trim();

        if (v.startsWith("Дата ")) {
            return null;
        }

        int spaceIndex = v.indexOf(' ');
        if (spaceIndex > 0) {
            v = v.substring(0, spaceIndex);
        }

        try {
            return LocalDate.parse(v, DATE_FORMATTER);
        } catch (Exception e) {
            System.out.println("Skip date parse, value='" + value + "'");
            return null;
        }
    }
}