package com.comp4321Project.searchEngine.Model;

public class Constants {
    private final static int extractTopKKeywords = 5;
    private final static String defaultDBPath = "rocksDBFiles";
    private final static String nextAvailableIdLiteral = "nextAvailableId";

    public static String getNextAvailableIdLiteral() {
        return nextAvailableIdLiteral;
    }

    public static int getExtractTopKKeywords() {
        return extractTopKKeywords;
    }

    public static String getDefaultDBPath() {
        return defaultDBPath;
    }
}

