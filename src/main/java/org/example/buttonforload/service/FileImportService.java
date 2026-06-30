package org.example.buttonforload.service;

import org.example.buttonforload.dto.ImportResultDto;
import org.example.buttonforload.dto.ResourceRowDto;
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

    public FileImportService(FileStorageService fileStorageService,
                             FileDownloadService fileDownloadService, XlsxParseService xlsxParseService) {
        this.fileStorageService = fileStorageService;
        this.fileDownloadService = fileDownloadService;
        this.xlsxParseService = xlsxParseService;
    }

    public ImportResultDto importFile() {


        byte[] content = fileDownloadService.download(sourceUrl);

        String version = "unknown";
        String extension = "xlsx";
        String fileName = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_" + version + "." + extension;

        Path savedPath = fileStorageService.save(content, fileName);
        List<ResourceRowDto> rows = xlsxParseService.parse(savedPath);

        return new ImportResultDto("Файл сохранен: " + savedPath + ", строк: " + rows.size());
    }
}
