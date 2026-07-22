package org.example.buttonforload.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.buttonforload.dto.ResourceRowDto;
import org.example.buttonforload.repository.ResourceRowRepository;
import org.example.buttonforload.service.XlsxParseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileImportScheduler {

    @Value("${app.files.storage-dir}")
    private String sourceUrl;

    @Value("${app.files.archive-dir}")
    private String archiveDir;

    @Value("${app.files.error-dir}")
    private String errorDir;

    private static final Logger log = LoggerFactory.getLogger(FileImportScheduler.class);

    private final XlsxParseService xlsxParseService;
    private final ResourceRowRepository resourceRowRepository;

    @Scheduled(fixedDelay = 60000)
    public void scanDirectory() {
        Path scanDirectoryPath = Paths.get(sourceUrl);

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

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        File oldFile = files[0];
        Path sourcePath = oldFile.toPath();

            try {
                log.info("Обработка файла в хронологическом порядке: {} (Время изменения: {})",
                        oldFile.getName(), oldFile.lastModified());

                List<ResourceRowDto> rows = xlsxParseService.parse(sourcePath);

                if (!rows.isEmpty()) {
                    int[] result = resourceRowRepository.batchInsert(rows);
                    log.info("Файл {} успешно импортирован в БД. Строк: {}", oldFile.getName(), result.length);
                } else {
                    log.warn("Файл {} пуст, пропускаем запись в БД.", oldFile.getName());
                }

                archiveFile(sourcePath, archiveDir);

            } catch (Exception e) {
                log.error("Ошибка при обработке файла: {}: {}", oldFile.getName(), e.getMessage(), e);
                try {
                    archiveFile(sourcePath, errorDir);
                } catch (IOException ioException) {
                    log.error("Не удалось переместить файл {}, файл поврежден! {}",
                            oldFile.getName(), ioException.getMessage(), ioException);
                }
            }
        }

    private void archiveFile(Path sourcePath, String targetDir) throws IOException {
        Path targetFolder = Paths.get(targetDir);

        if (!Files.exists(targetFolder)) {
            Files.createDirectories(targetFolder);
        }

        String fileName = sourcePath.getFileName().toString();
        Path targetPath = targetFolder.resolve(fileName);

        if (Files.exists(targetPath)) {
            String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String newFileName = nameWithoutExtension + "_" + System.currentTimeMillis() + extension;
            targetPath = targetFolder.resolve(newFileName);
        }

        Files.move(sourcePath, targetPath,  StandardCopyOption.REPLACE_EXISTING);
        log.info("Файл успешно перенесен в архив: {}", targetPath.getFileName());
    }
}
