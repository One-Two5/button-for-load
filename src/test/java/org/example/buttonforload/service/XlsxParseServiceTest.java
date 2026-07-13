package org.example.buttonforload.service;

import org.example.buttonforload.dto.ResourceRowDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XlsxParseServiceTest {

    private static XlsxParseService xlsxParseService;
    private static Path filePath;
    private static List<ResourceRowDto> result;

    @BeforeEach
    void setUp() {
        xlsxParseService = new XlsxParseService();
        filePath = Paths.get("src", "test", "resources", "export.xlsx");
        assertTrue(Files.exists(filePath), "Тестовый файл export.xlsx не найден!");
        result = xlsxParseService.parse(filePath);
    }

    @Test
    void shouldParseAllValidRowsFromXlsxFile() {

       assertNotNull(result, "Результат парсинга не должен быть null");
       assertFalse(result.isEmpty(), "Сервис должен содержать строки с данными");

       for (ResourceRowDto dto : result) {
           assertNotNull(dto.getNumber(), "Порядковый номер должен быть заполнен для всех записей");
           assertNotNull(dto.getFullName(), "Имя/Наименование агента не должно быть пустым");

           assertTrue(dto.getNumber() > 0, "Некорректный номер строки");
       }
    }

    @Test
    void shouldSkipHeaderRow() {

        boolean hasHeaderInResult = result.stream()
                .anyMatch(dto -> dto.getFullName() != null && dto.getFullName()
                        .contains("Полное наименование"));

        assertFalse(hasHeaderInResult, "Строка заголовков колонок должна быть пропущена");
    }

    @Test
    void shouldSetNullForMissingOptionalFields() {

        boolean nullField = false;

        for (ResourceRowDto dto : result) {

            if (dto.getFullName() != null) {
                assertFalse(dto.getFullName().isBlank(),
                        "Строковое поле не должно быть пустым, если оно не null");
            }
            if (dto.getInclusionGrounds() != null) {
                assertFalse(dto.getInclusionGrounds().isBlank(),
                        "Строковое поле не должно быть пустым, если оно не null");
            }

            if (dto.getFullName() == null
                    || dto.getInclusionGrounds() == null
                    || dto.getInclusionDecisionDate() == null
                    || dto.getExclusionDecisionDate() == null) {
                nullField = true;
            }
        }

        assertTrue(nullField,
                "В таблице нет пустых полей");
    }

    @Test
    void shouldThrowExceptionWhenFileDoesNotExist() {

        Path wrongFile = Paths.get("src", "test", "resources", "notExist.xlsx");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> xlsxParseService.parse(wrongFile));
        assertTrue(exception.getMessage().contains("Не удалось прочитать XLSX файл"));
    }
}
