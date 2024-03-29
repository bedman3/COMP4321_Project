package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Model.ProcessedQuery;
import com.comp4321Project.searchEngine.Service.BatchProcessing;
import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.QueryHistoryView;
import com.comp4321Project.searchEngine.View.QuerySearchResponseView;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
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
    private ColumnFamilyHandle urlIdToKeywordFrequencyForBodyRocksDBCol;
    private ColumnFamilyHandle urlIdToTop5KeywordRocksDBCol;
    private ColumnFamilyHandle invertedFileForBodyWordIdToPostingListRocksDBCol;
    private ColumnFamilyHandle invertedFileForTitleWordIdToPostingListRocksDBCol;
    private ColumnFamilyHandle urlIdToLastModifiedDateRocksDBCol;
    private ColumnFamilyHandle urlIdToKeywordTermFrequencyForBodyRocksDBCol;
    private ColumnFamilyHandle wordIdToDocumentFrequencyRocksDBCol;
    private ColumnFamilyHandle wordIdToInverseDocumentFrequencyRocksDBCol;
    private ColumnFamilyHandle urlIdToKeywordTFIDFVectorRocksDBCol;
    private ColumnFamilyHandle fetchedSiteHashSetRocksDBCol;
    private ColumnFamilyHandle urlIdToKeywordFrequencyForTitleRocksDBCol;
    private ColumnFamilyHandle urlIdToKeywordVectorForTitleRocksDBCol;
    private ColumnFamilyHandle urlIdToPageRankScoreRocksDBCol;
    private ColumnFamilyHandle queryHashToQueryCacheRocksDBCol;
    private ColumnFamilyHandle queryHistoryRocksDBCol;


    private InvertedFile invertedFileForBody;
    private InvertedFile invertedFileForTitle;
    private ColumnFamilyHandle miscellaneousCacheRocksDBCol;

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

    public ColumnFamilyHandle getMiscellaneousCacheRocksDBCol() {
        return miscellaneousCacheRocksDBCol;
    }

    public ColumnFamilyHandle getQueryHashToQueryCacheRocksDBCol() {
        return queryHashToQueryCacheRocksDBCol;
    }

    public ColumnFamilyHandle getQueryHistoryRocksDBCol() {
        return queryHistoryRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToKeywordFrequencyForTitleRocksDBCol() {
        return urlIdToKeywordFrequencyForTitleRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToKeywordVectorForTitleRocksDBCol() {
        return urlIdToKeywordVectorForTitleRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdToPageRankScoreRocksDBCol() {
        return urlIdToPageRankScoreRocksDBCol;
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
                new ColumnFamilyDescriptor("UrlIdToKeywordFrequencyForBodyData".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToTop5Keyword".getBytes()),
                new ColumnFamilyDescriptor("InvertedFileForBodyWordIdToPostingList".getBytes()),
                new ColumnFamilyDescriptor("InvertedFileForTitleWordIdToPostingList".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToLastModifiedDate".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToKeywordTermFrequencyData".getBytes()),
                new ColumnFamilyDescriptor("WordIdToDocumentFrequencyData".getBytes()),
                new ColumnFamilyDescriptor("WordIdToInverseDocumentFrequencyData".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToKeywordTFIDFVectorData".getBytes()),
                new ColumnFamilyDescriptor("FetchedSiteHashSetData".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToPageRankScoreData".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToKeywordFrequencyForTitleData".getBytes()),
                new ColumnFamilyDescriptor("UrlIdToKeywordVectorForTitleData".getBytes()),
                new ColumnFamilyDescriptor("QueryHashToQueryCacheData".getBytes()),
                new ColumnFamilyDescriptor("QueryHistoryData".getBytes()),
                new ColumnFamilyDescriptor("MiscellaneousCacheData".getBytes())
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
        this.urlIdToKeywordFrequencyForBodyRocksDBCol = colFamilyIt.next();
        this.urlIdToTop5KeywordRocksDBCol = colFamilyIt.next();
        this.invertedFileForBodyWordIdToPostingListRocksDBCol = colFamilyIt.next();
        this.invertedFileForTitleWordIdToPostingListRocksDBCol = colFamilyIt.next();
        this.urlIdToLastModifiedDateRocksDBCol = colFamilyIt.next();
        this.urlIdToKeywordTermFrequencyForBodyRocksDBCol = colFamilyIt.next();
        this.wordIdToDocumentFrequencyRocksDBCol = colFamilyIt.next();
        this.wordIdToInverseDocumentFrequencyRocksDBCol = colFamilyIt.next();
        this.urlIdToKeywordTFIDFVectorRocksDBCol = colFamilyIt.next();
        this.fetchedSiteHashSetRocksDBCol = colFamilyIt.next();
        this.urlIdToPageRankScoreRocksDBCol = colFamilyIt.next();
        this.urlIdToKeywordFrequencyForTitleRocksDBCol = colFamilyIt.next();
        this.urlIdToKeywordVectorForTitleRocksDBCol = colFamilyIt.next();
        this.queryHashToQueryCacheRocksDBCol = colFamilyIt.next();
        this.queryHistoryRocksDBCol = colFamilyIt.next();
        this.miscellaneousCacheRocksDBCol = colFamilyIt.next();

        this.invertedFileForBody = new InvertedFile(this, this.invertedFileForBodyWordIdToPostingListRocksDBCol);
        this.invertedFileForTitle = new InvertedFile(this, this.invertedFileForTitleWordIdToPostingListRocksDBCol);

        // init rocksdb for id data
        this.initRocksDBWithNextAvailableId(urlIdToUrlRocksDBCol);
        this.initRocksDBWithNextAvailableId(wordIdToWordRocksDBCol);
        this.initRocksDBWithNextAvailableId(queryHistoryRocksDBCol);

        this.initialized = true;
    }

    public ColumnFamilyHandle resetFetchSiteHashSet() throws RocksDBException {
        rocksDB.dropColumnFamily(this.fetchedSiteHashSetRocksDBCol);
        this.fetchedSiteHashSetRocksDBCol = rocksDB.createColumnFamily(new ColumnFamilyDescriptor("FetchedSiteHashSetData".getBytes()));
        return this.fetchedSiteHashSetRocksDBCol;
    }

    public void resetQueryResponseCache() throws RocksDBException {
        rocksDB.dropColumnFamily(this.queryHashToQueryCacheRocksDBCol);
        this.queryHashToQueryCacheRocksDBCol = rocksDB.createColumnFamily(new ColumnFamilyDescriptor("QueryHashToQueryCacheData".getBytes()));
    }

    public void resetMiscellaneousCache() throws RocksDBException {
        rocksDB.dropColumnFamily(this.miscellaneousCacheRocksDBCol);
        this.miscellaneousCacheRocksDBCol = rocksDB.createColumnFamily(new ColumnFamilyDescriptor("MiscellaneousCacheData".getBytes()));
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

    public ColumnFamilyHandle getUrlIdToKeywordTermFrequencyForBodyRocksDBCol() {
        return urlIdToKeywordTermFrequencyForBodyRocksDBCol;
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

    public ColumnFamilyHandle getUrlIdToKeywordFrequencyForBodyRocksDBCol() {
        return urlIdToKeywordFrequencyForBodyRocksDBCol;
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

    public void printAllDataFromRocksDBToTextFile() throws RocksDBException, IOException {
        printAllDataFromRocksDBToTextFile("rocksDBData.txt");
    }

    public void printAllDataFromRocksDBToTextFile(String filePath) throws RocksDBException, IOException {
        File writeFile = new File(filePath);

        if (writeFile.exists()) writeFile.delete();
        if (!writeFile.createNewFile()) return;

        PrintWriter writer = new PrintWriter(new FileWriter(writeFile));

        // print all storage
        for (ColumnFamilyHandle col : this.getColumnFamilyHandleList()) {
            RocksIterator it = rocksDB.newIterator(col);

            writer.println("ColumnFamily: " + new String(col.getName()));

            for (it.seekToFirst(); it.isValid(); it.next()) {
                String value;
                if (col == this.urlIdToKeywordFrequencyForBodyRocksDBCol ||
                        col == this.urlIdToKeywordTermFrequencyForBodyRocksDBCol ||
                        col == this.urlIdToKeywordTFIDFVectorRocksDBCol ||
                        col == this.urlIdToKeywordVectorForTitleRocksDBCol ||
                        col == this.urlIdToKeywordFrequencyForTitleRocksDBCol ||
                        col == this.invertedFileForBodyWordIdToPostingListRocksDBCol ||
                        col == this.invertedFileForTitleWordIdToPostingListRocksDBCol ||
                        col == this.childUrlIdToParentUrlIdRocksDBCol ||
                        col == this.parentUrlIdToChildUrlIdRocksDBCol ||
                        col == this.urlIdToMetaDataRocksDBCol) {
                    value = CustomFSTSerialization.getInstance().asObject(it.value()).toString();
                } else {
                    value = new String(it.value());
                }
                writer.println("key: " + new String(it.key()) + " | value: " + value);
            }

            writer.println();
        }
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

    public void putKeywordFrequencyDataForBody(String parentUrlId, Map<String, Integer> keyFreqMap) throws RocksDBException {
        rocksDB.put(this.urlIdToKeywordFrequencyForBodyRocksDBCol, parentUrlId.getBytes(), CustomFSTSerialization.getInstance().asByteArray(keyFreqMap));
    }

    public HashMap<String, Integer> getKeywordFrequencyDataForBody(byte[] urlIdByte) throws RocksDBException {
        return getKeywordFrequencyDataForBodyFromValue(rocksDB.get(this.urlIdToKeywordFrequencyForBodyRocksDBCol, urlIdByte));
    }


    public HashMap<String, Integer> getKeywordFrequencyDataForBodyFromValue(byte[] keywordFrequencyData) throws RocksDBException {
        if (keywordFrequencyData == null) return null;
        else return (HashMap<String, Integer>) CustomFSTSerialization.getInstance().asObject(keywordFrequencyData);
    }

    public void putKeywordTermFrequencyDataForBody(String parentUrlId, Map<String, Double> keyTermFreqMap) throws RocksDBException {
        rocksDB.put(this.urlIdToKeywordTermFrequencyForBodyRocksDBCol, parentUrlId.getBytes(), CustomFSTSerialization.getInstance().asByteArray(keyTermFreqMap));
    }

    public HashMap<String, Double> getKeywordTermFrequencyDataForBody(byte[] urlIdByte) throws RocksDBException {
        return getKeywordTermFrequencyDataForBodyFromValue(rocksDB.get(this.urlIdToKeywordTermFrequencyForBodyRocksDBCol, urlIdByte));
    }

    public HashMap<String, Double> getKeywordTermFrequencyDataForBodyFromValue(byte[] keywordTermFrequencyData) throws RocksDBException {
        if (keywordTermFrequencyData == null) return null;
        else return (HashMap<String, Double>) CustomFSTSerialization.getInstance().asObject(keywordTermFrequencyData);
    }

    public void putTfIdfScoreData(byte[] urlIdByte, ArrayList<ImmutablePair<String, Double>> vector) throws RocksDBException {
        rocksDB.put(this.urlIdToKeywordTFIDFVectorRocksDBCol, urlIdByte, CustomFSTSerialization.getInstance().asByteArray(vector));
    }

    /**
     * The tfidf vector is sorted by wordId string in asc order
     *
     * @param urlIdByte
     * @return
     * @throws RocksDBException
     */
    public ArrayList<ImmutablePair<String, Double>> getTfIdfScoreData(byte[] urlIdByte) throws RocksDBException {
        return getTfIdfScoreDataFromValue(rocksDB.get(this.urlIdToKeywordTFIDFVectorRocksDBCol, urlIdByte));
    }

    public ArrayList<ImmutablePair<String, Double>> getTfIdfScoreDataFromValue(byte[] tfIdfScoreByte) throws RocksDBException {
        if (tfIdfScoreByte == null) return null;
        else
            return (ArrayList<ImmutablePair<String, Double>>) CustomFSTSerialization.getInstance().asObject(tfIdfScoreByte);
    }

    public void putTop5KeywordData(String urlId, StringBuilder keyFreqTopKValue) throws RocksDBException {
        rocksDB.put(this.urlIdToTop5KeywordRocksDBCol, urlId.getBytes(), keyFreqTopKValue.toString().getBytes());
    }

    public void putKeywordFrequencyDataForTitle(String urlId, Map<String, Integer> keyFreqForTitleMap) throws RocksDBException {
        rocksDB.put(this.urlIdToKeywordFrequencyForTitleRocksDBCol, urlId.getBytes(), CustomFSTSerialization.getInstance().asByteArray(keyFreqForTitleMap));
    }

    public HashMap<String, Integer> getKeywordFrequencyDataForTitle(String urlId) throws RocksDBException {
        return getKeywordFrequencyDataForTitleFromByte(urlId.getBytes());
    }

    public HashMap<String, Integer> getKeywordFrequencyDataForTitleFromByte(byte[] urlIdByte) throws RocksDBException {
        byte[] keywordFrequencyDataForTitleByte = rocksDB.get(this.urlIdToKeywordFrequencyForTitleRocksDBCol, urlIdByte);
        if (keywordFrequencyDataForTitleByte == null) return null;
        else
            return (HashMap<String, Integer>) CustomFSTSerialization.getInstance().asObject(keywordFrequencyDataForTitleByte);
    }

    public void putKeywordVectorForTitle(byte[] urlId, ArrayList<ImmutablePair<String, Double>> keyFreqMap) throws RocksDBException {
        rocksDB.put(this.urlIdToKeywordVectorForTitleRocksDBCol, urlId, CustomFSTSerialization.getInstance().asByteArray(keyFreqMap));
    }

    public ArrayList<ImmutablePair<String, Double>> getKeywordFrequencyVectorForTitle(String urlId) throws RocksDBException {
        return getKeywordFrequencyVectorForTitleFromByte(urlId.getBytes());
    }

    public ArrayList<ImmutablePair<String, Double>> getKeywordFrequencyVectorForTitleFromByte(byte[] urlIdByte) throws RocksDBException {
        byte[] keywordFrequencyVectorForTitle = rocksDB.get(this.urlIdToKeywordVectorForTitleRocksDBCol, urlIdByte);
        if (keywordFrequencyVectorForTitle == null) return null;
        else
            return (ArrayList<ImmutablePair<String, Double>>) CustomFSTSerialization.getInstance().asObject(keywordFrequencyVectorForTitle);
    }

    public Double getPageRankScore(String urlId) throws RocksDBException {
        return getPageRankScoreFromByte(urlId.getBytes());
    }

    public Double getPageRankScoreFromByte(byte[] urlIdByte) throws RocksDBException {
        byte[] pageRankScoreByte = rocksDB.get(this.urlIdToPageRankScoreRocksDBCol, urlIdByte);
        if (pageRankScoreByte == null) return 0.0;
        return ByteBuffer.wrap(pageRankScoreByte).getDouble();
    }

    public void putParentUrlIdToChildUrlIdList(String urlId, HashSet<String> childLinkList) throws RocksDBException {
        rocksDB.put(this.parentUrlIdToChildUrlIdRocksDBCol, urlId.getBytes(), CustomFSTSerialization.getInstance().asByteArray(childLinkList));
    }

    public void putChildUrlIdToParentUrlIdList(String urlId, HashSet<String> parentList) throws RocksDBException {
        rocksDB.put(this.childUrlIdToParentUrlIdRocksDBCol, urlId.getBytes(), CustomFSTSerialization.getInstance().asByteArray(parentList));
    }

    public HashSet<String> getChildUrlIdToParentUrlIdList(String urlId) throws RocksDBException {
        byte[] list = rocksDB.get(this.childUrlIdToParentUrlIdRocksDBCol, urlId.getBytes());
        if (list == null) return null;
        else return (HashSet<String>) CustomFSTSerialization.getInstance().asObject(list);
    }

    public void putSiteMetaData(String urlId, SiteMetaData siteMetaData) throws RocksDBException {
        rocksDB.put(this.urlIdToMetaDataRocksDBCol, urlId.getBytes(), CustomFSTSerialization.getInstance().asByteArray(siteMetaData));
    }

    public SiteMetaData getSiteMetaData(String urlId) throws RocksDBException {
        byte[] siteMetaDataByte = rocksDB.get(this.urlIdToMetaDataRocksDBCol, urlId.getBytes());
        if (siteMetaDataByte == null) return null;
        else return (SiteMetaData) CustomFSTSerialization.getInstance().asObject(siteMetaDataByte);
    }

    public void putQueryCache(ProcessedQuery processedQuery, QuerySearchResponseView querySearchResponseView) throws RocksDBException {
        rocksDB.put(this.queryHashToQueryCacheRocksDBCol, Integer.toString(processedQuery.hashCode()).getBytes(), CustomFSTSerialization.getInstance().asByteArray(querySearchResponseView));
    }

    public QuerySearchResponseView getQueryCache(ProcessedQuery processedQuery) throws RocksDBException {
        byte[] queryCacheByte = rocksDB.get(this.queryHashToQueryCacheRocksDBCol, Integer.toString(processedQuery.hashCode()).getBytes());
        if (queryCacheByte == null) return null;
        else return (QuerySearchResponseView) CustomFSTSerialization.getInstance().asObject(queryCacheByte);
    }

    public void putStemmedKeywordCache(ArrayList<String> stemmedKeywordsList) throws RocksDBException {
        rocksDB.put(this.miscellaneousCacheRocksDBCol, "stemmedKeywords".getBytes(), CustomFSTSerialization.getInstance().asByteArray(stemmedKeywordsList));
    }

    public ArrayList<String> getStemmedKeywordCache() throws RocksDBException {
        byte[] cache = rocksDB.get(this.miscellaneousCacheRocksDBCol, "stemmedKeywords".getBytes());
        // if no cache is found, compute one and store it into cache
        if (cache == null) return BatchProcessing.getInstance(this).computeStemmedKeywordsList();
        return (ArrayList<String>) CustomFSTSerialization.getInstance().asObject(cache);
    }

    public void putQueryHistory(String rawQuery, QuerySearchResponseView querySearchResponseView) throws RocksDBException {
        byte[] nextAvailableIdByte = rocksDB.get(this.queryHistoryRocksDBCol, Constants.getNextAvailableIdLiteral().getBytes());
        Integer nextAvailableId = Integer.parseInt(new String(nextAvailableIdByte));
        rocksDB.put(this.queryHistoryRocksDBCol, Constants.getNextAvailableIdLiteral().getBytes(), (new Integer(nextAvailableId + 1)).toString().getBytes());

        rocksDB.put(this.queryHistoryRocksDBCol, nextAvailableId.toString().getBytes(),
                CustomFSTSerialization.getInstance().asByteArray(new QueryHistoryView(rawQuery, querySearchResponseView)));
    }

    public QueryHistoryView getQueryHistory(String historyId) throws RocksDBException {
        byte[] queryHistoryByte = rocksDB.get(this.queryHistoryRocksDBCol, historyId.getBytes());
        if (queryHistoryByte == null) return null;
        else return (QueryHistoryView) CustomFSTSerialization.getInstance().asObject(queryHistoryByte);
    }
}
