package com.comp4321Project.searchEngine.Util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {
    public static void createDirectoryIfNotExist(String path) {
        Path currDir = Paths.get("");
        Path checkDir = Paths.get(currDir.toString(), path);
        File createDir = new File(checkDir.toString());
        if (createDir.mkdirs()) {
            System.out.println("created directory: " + checkDir.toAbsolutePath().toString());
        } else {
            System.out.println("directory " + checkDir.toAbsolutePath().toString() + " exists");
        }
    }
}
