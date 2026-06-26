package org.example.buttonforload.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileNameGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String generateFileName(String version, String extension) {
        return  LocalDateTime.now().format(FORMATTER) + "_" + version + "." + extension;
    }
}
