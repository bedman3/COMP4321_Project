package com.comp4321Project.searchEngine.Model;

public class Constants {
    private final static int extractTopKKeywords = 5;
    private final static int maxReturnSearchResult = 50;
    private final static int invertedFileUpdateInterval = 500;
    private final static String defaultDBPath = "rocksDBFiles";
    private final static String nextAvailableIdLiteral = "nextAvailableId";
    private final static double ln2 = Math.log(2);
    private final static int maxConnectionRetry = 3;
    private final static int connectionTimeout = 10;

    public static double getTitleMultiplier() {
        return titleMultiplier;
    }

    private final static double titleMultiplier = 0.1;

    public static int getInvertedFileUpdateInterval() {
        return invertedFileUpdateInterval;
    }

    public static int getMaxConnectionRetry() {
        return maxConnectionRetry;
    }

    public static int getConnectionTimeout() {
        return connectionTimeout;
    }

    public static int getMaxReturnSearchResult() {
        return maxReturnSearchResult;
    }

    public static double getLn2() {
        return ln2;
    }

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

