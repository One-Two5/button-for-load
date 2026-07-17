package org.example.buttonforload.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.buttonforload.dto.ImportResultDto;
import org.example.buttonforload.service.FileImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileImportScheduler {

    private static final Logger log = LoggerFactory.getLogger(FileImportScheduler.class);
    private final FileImportService fileImportService;

    @Scheduled(fixedDelay = 60000)
    public void runScanTask() {
        log.info("Запуск автоматического импорта файлов...");
        try {
            ImportResultDto result = fileImportService.importFile();
            log.info("Автоматический импорт успешно завершен: {}", result);
        } catch (Exception e) {
            log.error("Ошибка при выполнении автоматического импорта: {}", e.getMessage());
        }
    }
}
