package org.example.buttonforload.service;

import org.example.buttonforload.dto.ImportResultDto;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileImportService {

    @Value("${app.external-service.url}")
    String sourceUrl;

    private final FileStorageService fileStorageService;
    private final FileDownloadService fileDownloadService;

    public FileImportService(FileStorageService fileStorageService,
                             FileDownloadService fileDownloadService) {
        this.fileStorageService = fileStorageService;
        this.fileDownloadService = fileDownloadService;
    }

    public ImportResultDto importFile() {
        String sourceUrl = "https://reestrs.minjust.gov.ru/rest/registry/39b95df9-9a68-6b6d-e1e3-e6388507067e/export?";
        byte[] content = fileDownloadService.download(sourceUrl);

        String version = "unknown";
        String extension = "xlsx";
        String fileName = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_" + version + "." + extension;

        Path savedPath = fileStorageService.save(content, fileName);
        return new ImportResultDto("Файл сохранен: " + savedPath);
    }
}
