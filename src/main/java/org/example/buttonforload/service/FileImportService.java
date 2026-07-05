package org.example.buttonforload.service;

import org.example.buttonforload.dto.ImportResultDto;
import org.example.buttonforload.dto.ResourceRowDto;
import org.example.buttonforload.repository.ResourceRowRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FileImportService {

    @Value("${app.external-service.url}")
    String sourceUrl;

    private final FileStorageService fileStorageService;
    private final FileDownloadService fileDownloadService;
    private final XlsxParseService xlsxParseService;
    private final ResourceRowRepository resourceRowRepository;

    public FileImportService(FileStorageService fileStorageService,
                             FileDownloadService fileDownloadService,
                             XlsxParseService xlsxParseService, ResourceRowRepository resourceRowRepository) {
        this.fileStorageService = fileStorageService;
        this.fileDownloadService = fileDownloadService;
        this.xlsxParseService = xlsxParseService;
        this.resourceRowRepository = resourceRowRepository;
    }

    public ImportResultDto importFile() {

        String extension = "xlsx";
        String version = "unknow";

        byte[] content = fileDownloadService.download(sourceUrl);

        String fileName = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_" + version + "." + extension;

        Path savedPath = fileStorageService.save(content, fileName);
        List<ResourceRowDto> rows = xlsxParseService.parse(savedPath);

        int[] result = resourceRowRepository.batchInsert(rows);

        return new ImportResultDto("Импорт завершен. Сохранено строк: %d".formatted(result.length));
    }
}
