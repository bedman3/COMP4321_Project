package com.comp4321Project.searchEngine.Util;

public class RocksDBColIndex {
    private final static String nextAvailableIdLiteral = "nextAvailableId";
    private final static String urlToIdKeyPrefix = "urlToId_";
    private final static String wordToIdKeyPrefix = "wordToId_";
    private final static String idToUrlKeyPrefix = "idToUrl_";
    private final static String idToWordKeyPrefix = "idToWord_";
    private final static String keyFreqTopKKeyPrefix = "topK_";

    public static String getNextAvailableIdLiteral() {
        return nextAvailableIdLiteral;
    }

    public static String getUrlToIdKey(String url) {
        return String.format("%s%s", urlToIdKeyPrefix, url);
    }

    public static String getIdToUrlKey(String urlId) {
        return String.format("%s%s", idToUrlKeyPrefix, urlId);
    }

    public static String getWordToIdKey(String word) {
        return String.format("%s%s", wordToIdKeyPrefix, word);
    }

    public static String getIdToWordKeyPrefix(String wordId) {
        return String.format("%s%s", idToWordKeyPrefix, wordId);
    }

    public static String getUrlToIdKeyPrefix() {
        return urlToIdKeyPrefix;
    }

    public static String getWordToIdKeyPrefix() {
        return wordToIdKeyPrefix;
    }

    public static String getIdToUrlKeyPrefix() {
        return idToUrlKeyPrefix;
    }

    public static String getIdToWordKeyPrefix() {
        return idToWordKeyPrefix;
    }
}
