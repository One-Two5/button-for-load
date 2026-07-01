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
    private static final int START_ROW_INDEX = 0;

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

            for (int target = 0; target < sheetCount; target++) {
                System.out.println("Sheet " + target + " = " + workbook.getSheetName(target));
            }

            Sheet sheet = workbook.getSheetAt(START_ROW_INDEX);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String numberStr = cellValue(formatter, row.getCell(0));
                String fullName = cellValue(formatter, row.getCell(1));
                String inclusionGrounds = cellValue(formatter, row.getCell(2));
                String inclusionDateStr = cellValue(formatter, row.getCell(3));
                String exclusionDateStr = cellValue(formatter, row.getCell(4));

                if (isAllBlank(numberStr, fullName, inclusionGrounds, inclusionDateStr, exclusionDateStr)) {
                    continue;
                }

                ResourceRowDto dto = new ResourceRowDto();
                dto.setNumber(parseIntegerSafe(numberStr));
                dto.setFullName(blankToNull(fullName));
                dto.setInclusionGrounds(blankToNull(inclusionGrounds));
                dto.setInclusionDecisionDate(parseDateSafe(inclusionDateStr));
                dto.setExclusionDecisionDate(parseDateSafe(exclusionDateStr));

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
        for (String value : values) {
            if (value != null && !value.isBlank()) {
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

        String cellValue = value.trim();

        if (cellValue.startsWith("Дата ")) {
            return null;
        }

        int spaceIndex = cellValue.indexOf(' ');
        if (spaceIndex > 0) {
            cellValue = cellValue.substring(0, spaceIndex);
        }

        try {
            return LocalDate.parse(cellValue, DATE_FORMATTER);
        } catch (Exception e) {
            System.out.println("Skip date parse, value='" + value + "'");
            return null;
        }
    }
}