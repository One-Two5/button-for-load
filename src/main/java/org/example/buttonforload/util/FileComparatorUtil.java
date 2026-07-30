package org.example.buttonforload.util;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Comparator;

@UtilityClass
public class FileComparatorUtil {

    public final Comparator<File> COMPARATOR = (firstFile, secondFile) -> {
        long first = firstFile.lastModified();
        long second = secondFile.lastModified();
        return Long.compare(first, second);
    };
}
