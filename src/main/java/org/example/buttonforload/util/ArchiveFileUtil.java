package org.example.buttonforload.util;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@UtilityClass
public class ArchiveFileUtil {

    private static final Logger log = LoggerFactory.getLogger(ArchiveFileUtil.class);

    public static void archiveFile(Path sourcePath, String targetDir) throws IOException {
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
