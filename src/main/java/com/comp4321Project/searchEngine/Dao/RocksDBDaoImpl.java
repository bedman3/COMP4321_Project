package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.Util.RocksDBUtil;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.rocksdb.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RocksDBDaoImpl implements RocksDBDao {
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

    private HashMap<String, SiteMetaData> searchResultViewHashMap;

    public RocksDBDaoImpl(String dbPath) throws RocksDBException {

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
                new ColumnFamilyDescriptor("UrlIdToTop5Keyword".getBytes())
//                new ColumnFamilyDescriptor("VSpaceModelIndexData".getBytes())
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


        // init rocksdb for id data
        RocksDBUtil.initRocksDBWithNextAvailableId(rocksDB, urlIdToUrlRocksDBCol);
        RocksDBUtil.initRocksDBWithNextAvailableId(rocksDB, wordIdToWordRocksDBCol);
    }

    public RocksDBDaoImpl() throws RocksDBException {
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
                System.err.println("key: " + new String(it.key()) + " | value: " + new String(it.value()));
            }

            System.err.println();
        }
    }
}
