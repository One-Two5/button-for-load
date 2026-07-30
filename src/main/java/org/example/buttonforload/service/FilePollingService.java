package org.example.buttonforload.service;

import lombok.RequiredArgsConstructor;
import org.example.buttonforload.dto.ResourceRowDto;
import org.example.buttonforload.repository.ResourceRowRepository;
import org.example.buttonforload.util.ArchiveFileUtil;
import org.example.buttonforload.util.FileComparatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilePollingService {

    @Value("${app.files.storage-dir}")
    private String storageDir;

    @Value("${app.files.archive-dir}")
    private String archiveDir;

    @Value("${app.files.error-dir}")
    private String errorDir;

    private static final Logger log = LoggerFactory.getLogger(FilePollingService.class);

    private final XlsxParseService xlsxParseService;
    private final ResourceRowRepository resourceRowRepository;

    public void scanDirectory() {
        Path scanDirectoryPath = Paths.get(storageDir);

        if (!Files.exists(scanDirectoryPath)) {
            try {
                Files.createDirectories(scanDirectoryPath);
                log.info("Папка для сканирования создана: {}", scanDirectoryPath);
            } catch (IOException e) {
                log.error("Не удалось создать папку для сканирования: {}", e.getMessage());
                return;
            }
        }

        File dir = scanDirectoryPath.toFile();
        File[] files = dir.listFiles(file -> file.isFile() && file.getName().endsWith(".xlsx"));

        if  (files == null || files.length == 0) {
            return;
        }

        Arrays.sort(files, FileComparatorUtil.COMPARATOR);

        for (File currentFile : files) {
            Path sourcePath = currentFile.toPath();

            try {
                log.info("Обработка файла в хронологическом порядке: {} (Время изменения: {})",
                        currentFile.getName(), currentFile.lastModified());

                List<ResourceRowDto> rows = xlsxParseService.parse(sourcePath);

                if (!rows.isEmpty()) {
                    int[] result = resourceRowRepository.batchInsert(rows);
                    log.info("Файл {} успешно импортирован в БД. Строк: {}", currentFile.getName(), result.length);
                } else {
                    log.warn("Файл {} пуст, пропускаем запись в БД.", currentFile.getName());
                }

                ArchiveFileUtil.archiveFile(sourcePath, archiveDir);

            } catch (Exception e) {
                log.error("Ошибка при обработке файла: {}: {}", currentFile.getName(), e.getMessage(), e);
                try {
                    ArchiveFileUtil.archiveFile(sourcePath, errorDir);
                } catch (IOException ioException) {
                    log.error("Не удалось переместить файл {}, файл поврежден! {}",
                            currentFile.getName(), ioException.getMessage(), ioException);
                }
            }
        }
        log.info("Пакетная обработка файлов завершена.");
    }
}
