package org.example.buttonforload.service;

import org.example.buttonforload.dto.ResourceRowDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XlsxParseServiceTest {

    private static XlsxParseService xlsxParseService;

    @BeforeEach
    void setUp() {
        xlsxParseService = new XlsxParseService();
    }

    @Test
    void shouldParseAllValidRowsFromXlsxFile() {
        Path filePath = Paths.get("src", "test", "resources", "tables", "parse_all_valid_rows.xlsx");
        List<ResourceRowDto> result = xlsxParseService.parse(filePath);

        assertEquals(2, result.size(), "Количество распарсенных строк не совпадает с ожидаемым");

        for (int i = 0; i < result.size(); i++) {
            ResourceRowDto row = result.get(i);
            int index = i;

            assertAll("Проверка полей для строки индекса " + index,
                    () -> assertNotNull(row.getNumber(), "Поле 'number' пустое в строке " + index),
                    () -> assertNotNull(row.getFullName(), "Поле 'fullName' пустое в строке " + index),
                    () -> assertNotNull(row.getInclusionGrounds(),
                            "Поле 'inclusionGrounds' пустое в строке " + index),
                    () -> assertNotNull(row.getInclusionDecisionDate(),
                            "Поле 'inclusionDecisionDate' пустое в строке " + index),
                    () -> assertNotNull(row.getExclusionDecisionDate(),
                            "Поле 'exclusionDecisionDate' пустое в строке " + index)
            );
        }
    }

    @Test
    void shouldSkipNullRowAndContinueParsing() {
        Path nullRow = Paths.get("src", "test", "resources", "tables", "skip_null_row.xlsx");

        assertTrue(java.nio.file.Files.exists(nullRow),
                "Тестовый файл skip_null_row.xlsx не найден по пути: " + nullRow.toAbsolutePath());

        List<ResourceRowDto> result = xlsxParseService.parse(nullRow);

        assertNotNull(result, "Результат парсинга не должен быть null");
        assertFalse(result.isEmpty(), "Результат не должен быть пустым, так как после пустой строки есть валидные данные");

        ResourceRowDto dto = result.get(0);
        assertNotNull(dto.getNumber(), "Порядковый номер успешно прочитанной строки должен быть заполнен");
        assertTrue(dto.getNumber() > 0, "Порядковый номер должен быть корректным числом больше нуля");
    }

    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {
        Path emptyFile = Paths.get("src", "test", "resources", "tables", "file_is_empty.xlsx");
        List<ResourceRowDto> result = xlsxParseService.parse(emptyFile);

        assertNotNull(result, "Сервис не должен возвращать null вместо списка");
        assertTrue(result.isEmpty(), "Список должен быть пустым для файла без строк");
    }

    @Test
    void shouldReturnNullWhenCellDoesNotExist() {
        Path missingCell = Paths.get("src", "test", "resources", "tables", "missing_cell.xlsx");
        assertTrue(Files.exists(missingCell), "Тестовый файл missing_cells.xlsx не найден по пути: "
                    + missingCell.toAbsolutePath());
        List<ResourceRowDto> result = xlsxParseService.parse(missingCell);

        assertNotNull(result, "Результат парсинга не должен быть null");
        assertEquals(5, result.size(),"Номер строки должен быть корректно прочитан");

        ResourceRowDto dto = result.get(0);
        assertEquals(5, dto.getNumber(),"Номер строки должен быть корректно прочитан");

        assertNull(dto.getFullName(), "Поле fullName должно быть null для отсутствующей ячейки");
        assertNull(dto.getInclusionGrounds(), "Поле inclusionGrounds должно быть null для отсутствующей ячейки");
        assertNull(dto.getInclusionDecisionDate(), "Поле даты включения должно быть null для отсутствующей ячейки");
        assertNull(dto.getExclusionDecisionDate(), "Поле даты исключения должно быть null для отсутствующей ячейки");
    }

    @Test
    void shouldReturnNullDateWhenCellValueStartsWithDatePrefix() {
        Path datePrefix = Paths.get("src", "test", "resources", "tables", "cell_value_starts_with_date_prefix.xlsx");

        assertTrue(Files.exists(datePrefix), "Тестовый файл cell_value_starts_with_date_prefix.xlsx не найден по пути: "
                    + datePrefix.toAbsolutePath());

        List<ResourceRowDto> result = xlsxParseService.parse(datePrefix);

        assertNotNull(result, "Результат парсинга не должен быть null");
        assertFalse(result.isEmpty(), "Строка должна быть успешно распарсена");

        ResourceRowDto dto = result.get(0);

        assertNull(dto.getInclusionDecisionDate(), "Если текст в ячейке начинается с 'Дата '," +
                " метод parseDateSafe должен вернуть null");
    }
//    перейти к следующему условию в парсинге дат — отсечению времени по первому пробелу (if (spaceIndex > 0))
    @Test
    void shouldTruncateTimeAndParseDateCorrectlyFormResource() {
        Path truncateTime = Paths.get("src", "test", "resources", "tables", "truncate_time.xlsx");

        List<ResourceRowDto> result = xlsxParseService.parse(truncateTime);

        assertNotNull(result);
        assertEquals(5, result.size(), "Должна успешно распарситься одна строка");

        ResourceRowDto dto = result.get(0);

        assertEquals(LocalDate.of(2014, 06, 05), dto.getInclusionDecisionDate(),
                "Дата включения должна быть распарсена без времени");
        assertEquals(LocalDate.of(2017, 02, 20), dto.getExclusionDecisionDate(),
                "Дата исключения должна быть распарсена без времени");
    }

    @Test
    void shouldThrowRuntimeExceptionWhenFileDoesNotExist() {
        Path doesNotExist = Paths.get("src", "test", "resources", "tables", "does_not_exist.xlsx");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> xlsxParseService.parse(doesNotExist));

        assertTrue(exception.getMessage().contains("Не удалось прочитать XLSX файл:"),
                "Сообщение об ошибке должно быть информативным");
        assertTrue(exception.getMessage().contains(doesNotExist.toString()),
                "Сообщение должно содержать путь к файлу");
    }
}
