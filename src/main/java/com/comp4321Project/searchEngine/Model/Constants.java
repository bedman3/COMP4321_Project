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
    private final static double titleMultiplier = 0.5;
    private final static int defaultNumOfQueryHistory = 50;
    private final static double pageRankMultiplier = 0.1;
    private final static int pageRankIteration = 100;
    private final static double dampingFactor = 0.85;

    public static double getPageRankMultiplier() {
        return pageRankMultiplier;
    }

    public static int getPageRankIteration() {
        return pageRankIteration;
    }

    public static double getDampingFactor() {
        return dampingFactor;
    }

    public static int getDefaultNumOfQueryHistory() {
        return defaultNumOfQueryHistory;
    }

    public static double getTitleMultiplier() {
        return titleMultiplier;
    }

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

