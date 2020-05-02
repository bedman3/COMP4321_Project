package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
@Scope("singleton")
public class RocksDBDao {
    private static RocksDBDao daoInstance; // singleton to avoid rocksdb file lock
    private boolean initialized;
    private String dbPath;
    private ColumnFamilyHandle defaultRocksDBCol;
    private List<ColumnFamilyHandle> columnFamilyHandleList;
    private RocksDB rocksDB;
    private ColumnFamilyHandle urlIdToMetaDataRocksDBCol;
    private ColumnFamilyHandle parentUrlIdToChildUrlIdRocksDBCol;
    private ColumnFamilyHandle childUrlIdToParentUrlIdRocksDBCol;
    private ColumnFamilyHandle urlToUrlIdRocksDBCol;
    private ColumnFamilyHandle urlIdToUrlRocksDBCol;
    private ColumnFamilyHandle wordToWordIdRocksDBCol;
    private ColumnFamilyHandle wordIdToWordRocksDBCol;
    private ColumnFamilyHandle urlIdToKeywordFrequencyRocksDBCol;
    private ColumnFamilyHandle urlIdToTop5KeywordRocksDBCol;
    private ColumnFamilyHandle invertedFileForBodyWordIdToPostingListRocksDBCol;
    private ColumnFamilyHandle invertedFileForTitleWordIdToPostingListRocksDBCol;
    private ColumnFamilyHandle urlIdToLastModifiedDateRocksDBCol;
    private ColumnFamilyHandle urlIdToKeywordTermFrequencyRocksDBCol;
    private ColumnFamilyHandle wordIdToDocumentFrequencyRocksDBCol;
    private ColumnFamilyHandle wordIdToInverseDocumentFrequencyRocksDBCol;
    private ColumnFamilyHandle urlIdToKeywordTFIDFVectorRocksDBCol;
    private ColumnFamilyHandle fetchedSiteHashSetRocksDBCol;
    private InvertedFile invertedFileForBody;
    private InvertedFile invertedFileForTitle;

    private RocksDBDao(@Value("${rocksdb.folder}") String relativeDBPath) throws RocksDBException {
        this.dbPath = relativeDBPath;
    }

    public static RocksDBDao getInstance(String dbPath) throws RocksDBException {
        if (daoInstance == null) {
            daoInstance = new RocksDBDao(dbPath);
            daoInstance.initialize();
        }

        return daoInstance;
    }

    /**
     * Singleton design
     *
     * @return RocksDB instance
     * @throws RocksDBException
     */
    public static RocksDBDao getInstance() throws RocksDBException {
        return getInstance(Constants.getDefaultDBPath());
    }

    public ColumnFamilyHandle getFetchedSiteHashSetRocksDBCol() {
        return fetchedSiteHashSetRocksDBCol;
    }

    @PostConstruct
    private void initialize() throws RocksDBException {
        Util.createDirectoryIfNotExist(dbPath);

        // delete LOCK file if exists
        Util.deleteRocksDBLockFile(dbPath);

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
                new ColumnFamilyDescriptor("UrlIdToKeywordTFIDFVectorData".getBytes()),
                new ColumnFamilyDescriptor("FetchedSiteHashSetData".getBytes())
        );
        this.columnFamilyHandleList = new ArrayList<>();

        this.rocksDB = RocksDB.open(dbOptions, dbPath, columnFamilyDescriptorList, this.columnFamilyHandleList);

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
        this.urlIdToKeywordTFIDFVectorRocksDBCol = colFamilyIt.next();
        this.fetchedSiteHashSetRocksDBCol = colFamilyIt.next();

        this.invertedFileForBody = new InvertedFile(this, this.invertedFileForBodyWordIdToPostingListRocksDBCol);
        this.invertedFileForTitle = new InvertedFile(this, this.invertedFileForTitleWordIdToPostingListRocksDBCol);

        // init rocksdb for id data
        this.initRocksDBWithNextAvailableId(urlIdToUrlRocksDBCol);
        this.initRocksDBWithNextAvailableId(wordIdToWordRocksDBCol);

        this.initialized = true;
    }

    public ColumnFamilyHandle recreateFetchSiteHashSet() throws RocksDBException {
        rocksDB.dropColumnFamily(this.fetchedSiteHashSetRocksDBCol);
        this.fetchedSiteHashSetRocksDBCol = rocksDB.createColumnFamily(new ColumnFamilyDescriptor("FetchedSiteHashSetData".getBytes()));
        return this.fetchedSiteHashSetRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToUrlRocksDBCol() {
        return urlIdToUrlRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToKeywordTFIDFVectorRocksDBCol() {
        return urlIdToKeywordTFIDFVectorRocksDBCol;
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
        return getSiteSearchViewWithUrlIdFromByte(urlId.getBytes());
    }

    public SiteMetaData getSiteSearchViewWithUrlIdFromByte(byte[] urlIdByte) throws RocksDBException {
        // get meta data
        byte[] metaDataStringByte = this.rocksDB.get(urlIdToMetaDataRocksDBCol, urlIdByte);
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
                } else if (col == this.urlIdToKeywordFrequencyRocksDBCol || col == urlIdToKeywordTermFrequencyRocksDBCol || col == urlIdToKeywordTFIDFVectorRocksDBCol) {
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
        } else {
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
        rocksDB.put(this.urlIdToKeywordTFIDFVectorRocksDBCol, urlIdByte, CustomFSTSerialization.getInstance().asByteArray(vector));
    }

    public HashMap<String, Double> getTfIdfScoreData(byte[] urlIdByte) throws RocksDBException {
        return getTfIdfScoreDataFromValue(rocksDB.get(this.urlIdToKeywordTFIDFVectorRocksDBCol, urlIdByte));
    }

    public HashMap<String, Double> getTfIdfScoreDataFromValue(byte[] tfIdfScoreByte) throws RocksDBException {
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
