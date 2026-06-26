package org.example.buttonforload.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    @Value("${app.files.storage-dir:files}")
    private String storageDir;

    public Path save(byte[] content, String fileName) {
        try {
            Path dir = Paths.get(storageDir);
            Files.createDirectories(dir);

            Path filePath = dir.resolve(fileName);
            Files.write(filePath, content);

            if (!Files.exists(filePath)) {
                throw new IllegalStateException("Файл не был создан: " + filePath);
            }

            return filePath;

        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
    }
}
