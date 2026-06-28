package org.example.buttonforload.service;

import org.example.buttonforload.dto.ImportResultDto;
import org.example.buttonforload.util.FileNameGenerator;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class FileImportService {

    private final FileStorageService fileStorageService;
    private final FileDownloadService fileDownloadService;

    public FileImportService(FileStorageService fileStorageService,
                             FileDownloadService fileDownloadService) {
        this.fileStorageService = fileStorageService;
        this.fileDownloadService = fileDownloadService;
    }

    public ImportResultDto importFile() {
        String sourceUrl = "https://raw.githubusercontent.com/stedy/Machine-Learning-with-R-datasets/master/insurance.csv";
        byte[] content = fileDownloadService.download(sourceUrl);

        String version = "unknown";
        String extension = "xlsx";
        String fileName = FileNameGenerator.generateFileName(version, extension);

        Path savedPath = fileStorageService.save(content, fileName);
        return new ImportResultDto("Файл сохранен: " + savedPath);
    }
}
