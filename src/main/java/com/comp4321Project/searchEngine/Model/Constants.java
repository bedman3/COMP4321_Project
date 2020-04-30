package com.comp4321Project.searchEngine.Model;

public class Constants {
    private final static int extractTopKKeywords = 5;
    private final static String defaultDBPath = "rocksDBFiles";

    public static int getExtractTopKKeywords() {
        return extractTopKKeywords;
    }

    public static String getDefaultDBPath() {
        return defaultDBPath;
    }
}
