package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Model.PostingList;
import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;
import com.comp4321Project.searchEngine.Util.RocksDBColIndex;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.rocksdb.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RocksDBDao {
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
    private final ColumnFamilyHandle urlIdToTop5Keyword;
    private final ColumnFamilyHandle invertedFileForBodyWordIdToPostingList;
    private final ColumnFamilyHandle invertedFileForTitleWordIdToPostingList;

    private final InvertedFile invertedFileForBody;
    private InvertedFile invertedFileForTitle;
    private HashMap<String, SiteMetaData> searchResultViewHashMap;

    public RocksDBDao(String dbPath) throws RocksDBException {

        Util.createDirectoryIfNotExist(dbPath);

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
                new ColumnFamilyDescriptor("invertedFileForBodyWordIdToPostingList".getBytes()),
                new ColumnFamilyDescriptor("invertedFileForTitleWordIdToPostingList".getBytes())
        );
        this.columnFamilyHandleList = new ArrayList<>();

        this.rocksDB = RocksDB.open(dbOptions, dbPath, columnFamilyDescriptorList, this.columnFamilyHandleList);

        this.defaultRocksDBCol = columnFamilyHandleList.get(0);
        this.urlIdToMetaDataRocksDBCol = columnFamilyHandleList.get(1);
        this.parentUrlIdToChildUrlIdRocksDBCol = columnFamilyHandleList.get(2);
        this.childUrlIdToParentUrlIdRocksDBCol = columnFamilyHandleList.get(3);
        this.urlToUrlIdRocksDBCol = columnFamilyHandleList.get(4);
        this.urlIdToUrlRocksDBCol = columnFamilyHandleList.get(5);
        this.wordToWordIdRocksDBCol = columnFamilyHandleList.get(6);
        this.wordIdToWordRocksDBCol = columnFamilyHandleList.get(7);
        this.urlIdToKeywordFrequencyRocksDBCol = columnFamilyHandleList.get(8);
        this.urlIdToTop5Keyword = columnFamilyHandleList.get(9);
        this.invertedFileForBodyWordIdToPostingList = columnFamilyHandleList.get(10);
        this.invertedFileForTitleWordIdToPostingList = columnFamilyHandleList.get(11);

        this.invertedFileForBody = new InvertedFile(this, this.invertedFileForBodyWordIdToPostingList);

        // init rocksdb for id data
        this.initRocksDBWithNextAvailableId(urlIdToUrlRocksDBCol);
        this.initRocksDBWithNextAvailableId(wordIdToWordRocksDBCol);
    }


    public RocksDBDao() throws RocksDBException {
        this("rocksDBFiles");
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

    public ColumnFamilyHandle getUrlIdToUrlRocksDBCol() {
        return urlIdToUrlRocksDBCol;
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

    public ColumnFamilyHandle getUrlIdToTop5Keyword() {
        return urlIdToTop5Keyword;
    }

    public ColumnFamilyHandle getInvertedFileForBodyWordIdToPostingList() {
        return invertedFileForBodyWordIdToPostingList;
    }

    public ColumnFamilyHandle getInvertedFileForTitleWordIdToPostingList() {
        return invertedFileForTitleWordIdToPostingList;
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
                if (col == this.invertedFileForBodyWordIdToPostingList || col == this.invertedFileForTitleWordIdToPostingList) {
                    value = CustomFSTSerialization.getInstance().asObject(it.value()).toString();
                } else if (col == this.urlIdToKeywordFrequencyRocksDBCol) {
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
        byte[] nextAvailableIdByte = rocksDB.get(colHandle, RocksDBColIndex.getNextAvailableIdLiteral().getBytes());

        if (nextAvailableIdByte == null) {
            // if next available id field does not exist, assumes there is no document in it and we start from 0
            rocksDB.put(colHandle, new WriteOptions().setSync(true), RocksDBColIndex.getNextAvailableIdLiteral().getBytes(), "0".getBytes());
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
            byte[] nextAvailableIdStringByte = rocksDB.get(idToKeyColHandle, RocksDBColIndex.getNextAvailableIdLiteral().getBytes());
            Integer nextAvailableId = Integer.parseInt(new String(nextAvailableIdStringByte));
            rocksDB.put(idToKeyColHandle, RocksDBColIndex.getNextAvailableIdLiteral().getBytes(), (new Integer(nextAvailableId + 1)).toString().getBytes());

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
}
