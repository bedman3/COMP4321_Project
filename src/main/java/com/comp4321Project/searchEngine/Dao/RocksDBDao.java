package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.rocksdb.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RocksDBDao {
    private static RocksDBDao daoInstance; // singleton to avoid rocksdb file lock
    private final ColumnFamilyHandle defaultRocksDBCol;
    private final List<ColumnFamilyHandle> columnFamilyHandleList;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle urlIdToMetaDataRocksDBCol;
    private final ColumnFamilyHandle parentUrlIdToChildUrlIdRocksDBCol;
    private final ColumnFamilyHandle childUrlIdToParentUrlIdRocksDBCol;
    private final ColumnFamilyHandle urlToUrlIdRocksDBCol;
    private final ColumnFamilyHandle urlIdToUrlRocksDBCol;
    private final ColumnFamilyHandle wordToWordIdRocksDBCol;
    private final ColumnFamilyHandle wordIdToWordRocksDBCol;
    private final ColumnFamilyHandle urlIdToKeywordFrequencyRocksDBCol;
    private final ColumnFamilyHandle urlIdToTop5KeywordRocksDBCol;
    private final ColumnFamilyHandle invertedFileForBodyWordIdToPostingListRocksDBCol;
    private final ColumnFamilyHandle invertedFileForTitleWordIdToPostingListRocksDBCol;
    private final ColumnFamilyHandle urlIdToLastModifiedDateRocksDBCol;
    private final ColumnFamilyHandle urlIdToKeywordTermFrequencyRocksDBCol;
    private final ColumnFamilyHandle wordIdToDocumentFrequencyRocksDBCol;
    private final ColumnFamilyHandle wordIdToInverseDocumentFrequencyRocksDBCol;
    private final ColumnFamilyHandle urlIdToKeywordTFIDFVectorData;
    private final InvertedFile invertedFileForBody;
    private final InvertedFile invertedFileForTitle;
    private RocksDBDao(String relativeDBPath) throws RocksDBException {
        Util.createDirectoryIfNotExist(relativeDBPath);

        // delete LOCK file if exists
        Util.deleteRocksDBLockFile(relativeDBPath);

        RocksDB.loadLibrary();

        // The Options class contains a set of configurable DB options
        // that determines the behaviour of the database.
        DBOptions dbOptions = new DBOptions();
        dbOptions.setCreateIfMissing(true);
        dbOptions.setCreateMissingColumnFamilies(true);

        List<ColumnFamilyDescriptor> columnFamilyDescriptorList = Arrays.asList(
                new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY),
                new ColumnFamilyDescriptor("UrlIdToMetaData".getBytes()),
                new ColumnFamilyDescriptor("ParentUrlIdToChildUrlIdData".getBytes()),
                new ColumnFamilyDescriptor("ChildUrlIdToParentUrlIdData".getBytes()),
                new ColumnFamilyDescriptor("UrlToUrlIdData".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToUrlData".getBytes()),
                new ColumnFamilyDescriptor("WordToWordIdData".getBytes()),
                new ColumnFamilyDescriptor("WordIdToWordData".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToKeywordFrequencyData".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToTop5Keyword".getBytes()),
                new ColumnFamilyDescriptor("InvertedFileForBodyWordIdToPostingList".getBytes()),
                new ColumnFamilyDescriptor("InvertedFileForTitleWordIdToPostingList".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToLastModifiedDate".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToKeywordTermFrequencyData".getBytes()),
                new ColumnFamilyDescriptor("WordIdToDocumentFrequencyData".getBytes()),
                new ColumnFamilyDescriptor("WordIdToInverseDocumentFrequencyData".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToKeywordTFIDFVectorData".getBytes())
        );
        this.columnFamilyHandleList = new ArrayList<>();

        this.rocksDB = RocksDB.open(dbOptions, relativeDBPath, columnFamilyDescriptorList, this.columnFamilyHandleList);

        Iterator<ColumnFamilyHandle> colFamilyIt = columnFamilyHandleList.iterator();

        this.defaultRocksDBCol = colFamilyIt.next();
        this.urlIdToMetaDataRocksDBCol = colFamilyIt.next();
        this.parentUrlIdToChildUrlIdRocksDBCol = colFamilyIt.next();
        this.childUrlIdToParentUrlIdRocksDBCol = colFamilyIt.next();
        this.urlToUrlIdRocksDBCol = colFamilyIt.next();
        this.urlIdToUrlRocksDBCol = colFamilyIt.next();
        this.wordToWordIdRocksDBCol = colFamilyIt.next();
        this.wordIdToWordRocksDBCol = colFamilyIt.next();
        this.urlIdToKeywordFrequencyRocksDBCol = colFamilyIt.next();
        this.urlIdToTop5KeywordRocksDBCol = colFamilyIt.next();
        this.invertedFileForBodyWordIdToPostingListRocksDBCol = colFamilyIt.next();
        this.invertedFileForTitleWordIdToPostingListRocksDBCol = colFamilyIt.next();
        this.urlIdToLastModifiedDateRocksDBCol = colFamilyIt.next();
        this.urlIdToKeywordTermFrequencyRocksDBCol = colFamilyIt.next();
        this.wordIdToDocumentFrequencyRocksDBCol = colFamilyIt.next();
        this.wordIdToInverseDocumentFrequencyRocksDBCol = colFamilyIt.next();
        this.urlIdToKeywordTFIDFVectorData = colFamilyIt.next();

        this.invertedFileForBody = new InvertedFile(this, this.invertedFileForBodyWordIdToPostingListRocksDBCol);
        this.invertedFileForTitle = new InvertedFile(this, this.invertedFileForTitleWordIdToPostingListRocksDBCol);

        // init rocksdb for id data
        this.initRocksDBWithNextAvailableId(urlIdToUrlRocksDBCol);
        this.initRocksDBWithNextAvailableId(wordIdToWordRocksDBCol);
    }

    public static RocksDBDao getInstance(String dbPath) throws RocksDBException {
        if (daoInstance == null) {
            daoInstance = new RocksDBDao(dbPath);
        }

        return daoInstance;
    }

    /**
     * Singleton design
     * @return RocksDB instance
     * @throws RocksDBException
     */
    public static RocksDBDao getInstance() throws RocksDBException {
        return getInstance(Constants.getDefaultDBPath());
    }

    public ColumnFamilyHandle getUrlIdToUrlRocksDBCol() {
        return urlIdToUrlRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToKeywordTFIDFVectorData() {
        return urlIdToKeywordTFIDFVectorData;
    }

    public ColumnFamilyHandle getWordIdToInverseDocumentFrequencyRocksDBCol() {
        return wordIdToInverseDocumentFrequencyRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToLastModifiedDateRocksDBCol() {
        return urlIdToLastModifiedDateRocksDBCol;
    }

    public ColumnFamilyHandle getWordIdToDocumentFrequencyRocksDBCol() {
        return wordIdToDocumentFrequencyRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToKeywordTermFrequencyRocksDBCol() {
        return urlIdToKeywordTermFrequencyRocksDBCol;
    }

    public RocksDB getRocksDB() {
        return rocksDB;
    }

    public ColumnFamilyHandle getDefaultRocksDBCol() {
        return defaultRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToMetaDataRocksDBCol() {
        return urlIdToMetaDataRocksDBCol;
    }

    public ColumnFamilyHandle getParentUrlIdToChildUrlIdRocksDBCol() {
        return parentUrlIdToChildUrlIdRocksDBCol;
    }

    public ColumnFamilyHandle getChildUrlIdToParentUrlIdRocksDBCol() {
        return childUrlIdToParentUrlIdRocksDBCol;
    }

    public ColumnFamilyHandle getUrlToUrlIdRocksDBCol() {
        return urlToUrlIdRocksDBCol;
    }


    public ColumnFamilyHandle getWordToWordIdRocksDBCol() {
        return wordToWordIdRocksDBCol;
    }

    public ColumnFamilyHandle getWordIdToWordRocksDBCol() {
        return wordIdToWordRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToKeywordFrequencyRocksDBCol() {
        return urlIdToKeywordFrequencyRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToTop5KeywordRocksDBCol() {
        return urlIdToTop5KeywordRocksDBCol;
    }

    public ColumnFamilyHandle getInvertedFileForBodyWordIdToPostingListRocksDBCol() {
        return invertedFileForBodyWordIdToPostingListRocksDBCol;
    }

    public ColumnFamilyHandle getInvertedFileForTitleWordIdToPostingListRocksDBCol() {
        return invertedFileForTitleWordIdToPostingListRocksDBCol;
    }

    public List<ColumnFamilyHandle> getColumnFamilyHandleList() {
        return columnFamilyHandleList;
    }

    public SiteMetaData getSiteSearchViewWithUrlId(String urlId) throws RocksDBException {
        // get meta data
        byte[] metaDataStringByte = this.rocksDB.get(urlIdToMetaDataRocksDBCol, urlId.getBytes());
        if (metaDataStringByte != null) {
            return SiteMetaData.fromMetaDataString(new String(metaDataStringByte));
        } else {
            return null;
        }
    }

    public void printAllDataInRocksDB() throws RocksDBException {
        // print all storage
        for (ColumnFamilyHandle col : this.getColumnFamilyHandleList()) {
            RocksIterator it = rocksDB.newIterator(col);

            System.err.println("ColumnFamily: " + new String(col.getName()));

            for (it.seekToFirst(); it.isValid(); it.next()) {
                String value;
                if (col == this.invertedFileForBodyWordIdToPostingListRocksDBCol || col == this.invertedFileForTitleWordIdToPostingListRocksDBCol) {
                    value = CustomFSTSerialization.getInstance().asObject(it.value()).toString();
                } else if (col == this.urlIdToKeywordFrequencyRocksDBCol || col == urlIdToKeywordTermFrequencyRocksDBCol || col == urlIdToKeywordTFIDFVectorData) {
                    value = CustomFSTSerialization.getInstance().asObject(it.value()).toString();
                } else {
                    value = new String(it.value());
                }
                System.err.println("key: " + new String(it.key()) + " | value: " + value);
            }

            System.err.println();
        }
    }

    public void closeRocksDB() throws RocksDBException {
        this.rocksDB.closeE();
    }

    public void initRocksDBWithNextAvailableId(ColumnFamilyHandle colHandle) throws RocksDBException {
        byte[] nextAvailableIdByte = rocksDB.get(colHandle, Constants.getNextAvailableIdLiteral().getBytes());

        if (nextAvailableIdByte == null) {
            // if next available id field does not exist, assumes there is no document in it and we start from 0
            rocksDB.put(colHandle, new WriteOptions().setSync(true), Constants.getNextAvailableIdLiteral().getBytes(), "0".getBytes());
        }
    }

    public String getUrlIdFromUrl(String url) throws RocksDBException {
        return getIdFromKey(this.urlToUrlIdRocksDBCol, this.urlIdToUrlRocksDBCol, url);
    }

    public String getWordIdFromWord(String word) throws RocksDBException {
        return getIdFromKey(this.wordToWordIdRocksDBCol, this.wordIdToWordRocksDBCol, word);
    }

    public String getUrlFromUrlId(String urlId) throws RocksDBException {
        return getKeyFromId(this.urlIdToUrlRocksDBCol, urlId);
    }

    public String getWordFromWordId(String wordId) throws RocksDBException {
        return getKeyFromId(this.wordIdToWordRocksDBCol, wordId);
    }

    /**
     * This function will check if the key exists in rocksdb, if not it will create one
     *
     * @param keyToIdColHandle
     * @param idToKeyColHandle
     * @param key
     * @return
     * @throws RocksDBException
     */
    public String getIdFromKey(ColumnFamilyHandle keyToIdColHandle, ColumnFamilyHandle idToKeyColHandle, String key) throws RocksDBException {
        byte[] idByte = this.rocksDB.get(keyToIdColHandle, key.getBytes());
        if (idByte == null) {
            // TODO: use merge mechanism in rocksdb to ensure the nextAvailableId is synchronized
            //  when running with multiple workers

            // get the next available id
            byte[] nextAvailableIdStringByte = rocksDB.get(idToKeyColHandle, Constants.getNextAvailableIdLiteral().getBytes());
            Integer nextAvailableId = Integer.parseInt(new String(nextAvailableIdStringByte));
            rocksDB.put(idToKeyColHandle, Constants.getNextAvailableIdLiteral().getBytes(), (new Integer(nextAvailableId + 1)).toString().getBytes());

            // register this url with the new unique id
            rocksDB.put(keyToIdColHandle, key.getBytes(), nextAvailableId.toString().getBytes());

            // register a reverse index
            rocksDB.put(idToKeyColHandle, nextAvailableId.toString().getBytes(), key.getBytes());

            return nextAvailableId.toString();
        } else {
            return new String(idByte);
        }
    }

    /**
     * This function assumes the required key has already ran through {@link #getIdFromKey(ColumnFamilyHandle, ColumnFamilyHandle, String)}
     *
     * @param idToKeyColHandle
     * @param id
     * @return
     * @throws RocksDBException
     */
    public String getKeyFromId(ColumnFamilyHandle idToKeyColHandle, String id) throws RocksDBException {
        return new String(this.rocksDB.get(idToKeyColHandle, id.getBytes()));
    }

    public InvertedFile getInvertedFileForBody() {
        return invertedFileForBody;
    }

    public InvertedFile getInvertedFileForTitle() {
        return invertedFileForTitle;
    }

    public boolean isIgnoreWithLastModifiedDate(String urlId, String newLastModified) throws RocksDBException {
        byte[] lastModifiedDateByteFromRocksDB = this.rocksDB.get(this.urlIdToLastModifiedDateRocksDBCol, urlId.getBytes());
        if (lastModifiedDateByteFromRocksDB != null) {
            // if there exists last modified date in rocksdb,
            // then compare the lastModifiedDateByteFromRocksDB and newLastModified
            String lastModifiedDateStringFromRocksDB = new String(lastModifiedDateByteFromRocksDB);
            ZonedDateTime lastModifiedDateObjectFromRocksDB = ZonedDateTime.parse(lastModifiedDateStringFromRocksDB, DateTimeFormatter.RFC_1123_DATE_TIME);
            ZonedDateTime newLastModifiedDateObject = ZonedDateTime.parse(newLastModified, DateTimeFormatter.RFC_1123_DATE_TIME);

            boolean isIgnore = lastModifiedDateObjectFromRocksDB.compareTo(newLastModifiedDateObject) >= 0;

            if (!isIgnore) {
                // overwrite the newest last modified date in rocksdb
                this.rocksDB.put(this.urlIdToLastModifiedDateRocksDBCol, urlId.getBytes(), newLastModified.getBytes());
            }

            return isIgnore;
        } else {
            // there doesn't exist a last modified date, record the last modified date in rocksdb,
            // and do not ignore this site
            this.rocksDB.put(this.urlIdToLastModifiedDateRocksDBCol, urlId.getBytes(), newLastModified.getBytes());

            return false;
        }
    }

    public int getTotalNumOfDocuments() throws RocksDBException {
        byte[] totalNumOfDocumentByte = this.rocksDB.get(this.urlIdToUrlRocksDBCol, Constants.getNextAvailableIdLiteral().getBytes());
        if (totalNumOfDocumentByte == null) {
            return 0;
        } else {
            String totalNumOfDocumentString = new String(totalNumOfDocumentByte);
            try {
                return Integer.parseInt(totalNumOfDocumentString);
            } catch (NumberFormatException e) {
                System.err.println("Cannot parse total number of documents, totalNumOfDocumentString: " + totalNumOfDocumentString);
                return 0;
            }
        }
    }

    public Double getInverseDocumentFrequency(String wordId) throws RocksDBException {
        return getInverseDocumentFrequencyFromByte(wordId.getBytes());
    }

    public Double getInverseDocumentFrequencyFromByte(byte[] wordIdByte) throws RocksDBException {
        byte[] totalNumOfDocWithTermByte = this.rocksDB.get(this.wordIdToDocumentFrequencyRocksDBCol, wordIdByte);
        int totalNumOfDocuments = this.getTotalNumOfDocuments();
        if (totalNumOfDocWithTermByte == null) {
            // no num of doc record for this term, means there is no doc associated with this term
            return 0.0;
        } else if (totalNumOfDocuments == 0) {
            // no document available in the database, skip processing and return 0
            return 0.0;
        }
        else {
            String totalNumOfDocWithTermString = new String(totalNumOfDocWithTermByte);
            try {
                int totalNumOfDocWithTerm = Integer.parseInt(totalNumOfDocWithTermString);
                return Util.computeIdf(totalNumOfDocuments, totalNumOfDocWithTerm);
            } catch (NumberFormatException e) {
                System.err.println("Cannot parse total number of documents with wordId " + new String(wordIdByte) + ", totalNumOfDocWithTermString: " + totalNumOfDocWithTermString);
                return 0.0;
            }
        }
    }

    public void updateInvertedFileInRocksDB() throws RocksDBException {
        this.invertedFileForBody.mergeExistingWithRocksDB();
        this.invertedFileForBody.flushToRocksDB();

        this.invertedFileForTitle.mergeExistingWithRocksDB();
        this.invertedFileForTitle.flushToRocksDB();
    }

    public void putKeywordFrequencyData(String parentUrlId, Map<String, Integer> keyFreqMap) throws RocksDBException {
        rocksDB.put(this.urlIdToKeywordFrequencyRocksDBCol, parentUrlId.getBytes(), CustomFSTSerialization.getInstance().asByteArray(keyFreqMap));
    }

    public HashMap<String, Integer> getKeywordFrequencyData(byte[] urlIdByte) throws RocksDBException {
        return getKeywordFrequencyDataFromValue(rocksDB.get(this.urlIdToKeywordFrequencyRocksDBCol, urlIdByte));
    }


    public HashMap<String, Integer> getKeywordFrequencyDataFromValue(byte[] keywordFrequencyData) throws RocksDBException {
        if (keywordFrequencyData == null) {
            return null;
        } else {
            return (HashMap<String, Integer>) CustomFSTSerialization.getInstance().asObject(keywordFrequencyData);
        }
    }

    public void putKeywordTermFrequencyData(String parentUrlId, Map<String, Double> keyTermFreqMap) throws RocksDBException {
        rocksDB.put(this.urlIdToKeywordTermFrequencyRocksDBCol, parentUrlId.getBytes(), CustomFSTSerialization.getInstance().asByteArray(keyTermFreqMap));
    }

    public HashMap<String, Double> getKeywordTermFrequencyData(byte[] urlIdByte) throws RocksDBException {
        return getKeywordTermFrequencyDataFromValue(rocksDB.get(this.urlIdToKeywordTermFrequencyRocksDBCol, urlIdByte));
    }

    public HashMap<String, Double> getKeywordTermFrequencyDataFromValue(byte[] keywordTermFrequencyData) throws RocksDBException {
        if (keywordTermFrequencyData == null) {
            return null;
        } else {
            return (HashMap<String, Double>) CustomFSTSerialization.getInstance().asObject(keywordTermFrequencyData);
        }
    }

    public void putTfIdfScoreData(byte[] urlIdByte, HashMap<String, Double> vector) throws RocksDBException {
        rocksDB.put(this.urlIdToKeywordTFIDFVectorData, urlIdByte, CustomFSTSerialization.getInstance().asByteArray(vector));
    }

    public HashMap<String, Double> getTfIdfScoreData(String urlId) throws RocksDBException {
        return getTfIdfScoreDataFromByte(rocksDB.get(this.urlIdToKeywordTFIDFVectorData, urlId.getBytes()));
    }

    public HashMap<String, Double> getTfIdfScoreDataFromByte(byte[] tfIdfScoreByte) throws RocksDBException {
        if (tfIdfScoreByte == null) {
            return null;
        } else {
            return (HashMap<String, Double>) CustomFSTSerialization.getInstance().asObject(tfIdfScoreByte);
        }
    }

    public void putTop5KeywordData(String parentUrlId, StringBuilder keyFreqTopKValue) throws RocksDBException {
        rocksDB.put(this.urlIdToTop5KeywordRocksDBCol, parentUrlId.getBytes(), keyFreqTopKValue.toString().getBytes());
    }
}
