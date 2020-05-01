package com.comp4321Project.searchEngine.Util;

import com.comp4321Project.searchEngine.Model.Constants;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {
    public static void createDirectoryIfNotExist(String path) {
        Path currDir = Paths.get("");
        Path checkDir = Paths.get(currDir.toString(), path);
        File createDir = new File(checkDir.toString());
        if (createDir.mkdirs()) {
            System.err.println("created directory: " + checkDir.toAbsolutePath().toString());
        } else {
            System.err.println("directory " + checkDir.toAbsolutePath().toString() + " exists");
        }
    }

    public static void deleteRocksDBLockFile(String dbPath) {
        Path currDir = Paths.get("");
        Path checkDir = Paths.get(currDir.toString(), dbPath, "LOCK");
        File lockFile = new File(checkDir.toString());
        if (lockFile.exists()) {
            System.err.println("RocksDB LOCK file exists, try to remove it now");
            if (lockFile.delete()) {
                System.err.println("RocksDB LOCK file removed");
            } else {
                System.err.println("RocksDB LOCK file removal not successful");
            }
        } else {
            System.err.println("RocksDB LOCK file not found, proceed to next stage");
        }
    }

    public static double computeIdf(double totalNumOfDocuments, double totalNumOfDocWithTerm) {
        if (totalNumOfDocuments < 1 || totalNumOfDocWithTerm < 1) {
            return 0;
        }

        return Math.log(totalNumOfDocuments / totalNumOfDocWithTerm) / Constants.getLn2();
    }
}
