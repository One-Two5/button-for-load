package org.example.buttonforload.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.buttonforload.dto.ResourceRowDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XlsxParseServiceTest {

    private XlsxParseService xlsxParseService;

    @BeforeEach
    void setUp() {
        xlsxParseService = new XlsxParseService();
    }

    @Test
    void parse_ValidXlsx_ReturnsMappedDtoList(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("valid_data.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            sheet.createRow(0);

            Row headerRow = sheet.createRow(1);
            headerRow.createCell(0).setCellValue("№ п/п");
            headerRow.createCell(1).setCellValue("Полное наименование");
            headerRow.createCell(2).setCellValue("Основания");
            headerRow.createCell(3).setCellValue("Дата включения");
            headerRow.createCell(4).setCellValue("Дата исключения");

            Row firstRow = sheet.createRow(2);
            firstRow.createCell(0).setCellValue(1);
            firstRow.createCell(1).setCellValue("«Евразийская антимонопольная ассоциация»");
            firstRow.createCell(2).setCellValue("Статья 32 ФЗ");
            firstRow.createCell(3).setCellValue("27.06.2013");
            firstRow.createCell(4).setCellValue("05.07.2023");

            Row secondRow = sheet.createRow(3);
            secondRow.createCell(0).setCellValue(2);
            secondRow.createCell(1).setCellValue("Ассоциация ГОЛОС");
            secondRow.createCell(2).setCellValue("Статья 32 ФЗ");
            secondRow.createCell(3).setCellValue("05.06.2014");
            secondRow.createCell(4).setCellValue("");

            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }

        List<ResourceRowDto> result = xlsxParseService.parse(filePath);

        assertNotNull(result);
        assertEquals(2, result.size());

        ResourceRowDto firstDto = result.get(0);
        assertEquals(1, firstDto.getNumber());
        assertEquals("«Евразийская антимонопольная ассоциация»", firstDto.getFullName());
        assertEquals("Статья 32 ФЗ", firstDto.getInclusionGrounds());
        assertEquals(LocalDate.of(2013, 6, 27), firstDto.getInclusionDecisionDate());
        assertEquals(LocalDate.of(2023, 7, 5), firstDto.getExclusionDecisionDate());

        ResourceRowDto secondDto = result.get(1);
        assertEquals(2, secondDto.getNumber());
        assertEquals("Ассоциация ГОЛОС", secondDto.getFullName());
        assertNull(secondDto.getExclusionDecisionDate());
    }

    @Test
    void  parse_RowWithTextInIndexCell_IsSkipped(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("invalid_row.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            Row headerRow = sheet.createRow(1);
            headerRow.createCell(0).setCellValue("Это заголовок, тут нет числа");
            headerRow.createCell(1).setCellValue("Тестовое имя");

            Row dataRow = sheet.createRow(2);
            dataRow.createCell(0).setCellValue(1);
            dataRow.createCell(1).setCellValue("Корректное имя");

            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }

        List<ResourceRowDto> result = xlsxParseService.parse(filePath);

        assertEquals(1, result.size());
        assertEquals("Корректное имя", result.get(0).getFullName());
    }

    @Test
    void parse_CorruptedOrEmptyFile_ThrowsRuntimeException(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("empty.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }

        RuntimeException exception = assertThrows(RuntimeException.class, () -> xlsxParseService.parse(filePath));

        assertTrue(exception.getMessage().contains("Не удалось прочитать XLSX файл"));
    }

    @Test
    void parse_RowWithOnlyIndexAndNoData_IsSkipped(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("blank_row.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            Row dataRow = sheet.createRow(2);
            dataRow.createCell(0).setCellValue(999);
            dataRow.createCell(1).setCellValue("");
            dataRow.createCell(2).setCellValue("   "); // пробелы

            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }

        List<ResourceRowDto> result = xlsxParseService.parse(filePath);

        assertTrue(result.isEmpty() || result.get(0).getFullName() == null);
    }
}
