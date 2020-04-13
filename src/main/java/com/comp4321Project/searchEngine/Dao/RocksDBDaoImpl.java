package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.Util.RocksDBColIndex;
import com.comp4321Project.searchEngine.Util.RocksDBUtil;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.SearchResultView;
import org.rocksdb.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RocksDBDaoImpl implements RocksDBDao {
    private final ColumnFamilyHandle defaultRocksDBCol;
    private final ColumnFamilyHandle websiteMetaDataRocksDBCol;
    private final ColumnFamilyHandle vSpaceModelIndexDataRocksDBCol;
    private final ColumnFamilyHandle siteMapDataRocksDBCol;
    private final ColumnFamilyHandle urlIdDataRocksDBCol;
    private final ColumnFamilyHandle wordIdDataRocksDBCol;
    private final ColumnFamilyHandle keywordFrequencyDataRocksDBCol;
    private final List<ColumnFamilyHandle> columnFamilyHandleList;
    private final RocksDB rocksDB;
    private HashMap<String, SearchResultView> searchResultViewHashMap;

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
                new ColumnFamilyDescriptor("WebsiteMetaData".getBytes()),
                new ColumnFamilyDescriptor("VSpaceModelIndexData".getBytes()),
                new ColumnFamilyDescriptor("SiteMapData".getBytes()),
                new ColumnFamilyDescriptor("urlIdData".getBytes()),
                new ColumnFamilyDescriptor("wordIdData".getBytes()),
                new ColumnFamilyDescriptor("keywordFrequencyData".getBytes())
        );
        this.columnFamilyHandleList = new ArrayList<>();

        this.rocksDB = RocksDB.open(dbOptions, dbPath, columnFamilyDescriptorList, this.columnFamilyHandleList);

        this.defaultRocksDBCol = columnFamilyHandleList.get(0);
        this.websiteMetaDataRocksDBCol = columnFamilyHandleList.get(1);
        this.vSpaceModelIndexDataRocksDBCol = columnFamilyHandleList.get(2);
        this.siteMapDataRocksDBCol = columnFamilyHandleList.get(3);
        this.urlIdDataRocksDBCol = columnFamilyHandleList.get(4);
        this.wordIdDataRocksDBCol = columnFamilyHandleList.get(5);
        this.keywordFrequencyDataRocksDBCol = columnFamilyHandleList.get(6);

        // init rocksdb for id data
        RocksDBUtil.initRocksDBWithNextAvailableId(rocksDB, urlIdDataRocksDBCol);
        RocksDBUtil.initRocksDBWithNextAvailableId(rocksDB, wordIdDataRocksDBCol);
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

    public ColumnFamilyHandle getWebsiteMetaDataRocksDBCol() {
        return websiteMetaDataRocksDBCol;
    }

    public ColumnFamilyHandle getvSpaceModelIndexDataRocksDBCol() {
        return vSpaceModelIndexDataRocksDBCol;
    }

    public ColumnFamilyHandle getSiteMapDataRocksDBCol() {
        return siteMapDataRocksDBCol;
    }

    public ColumnFamilyHandle getKeywordFrequencyDataRocksDBCol() {
        return keywordFrequencyDataRocksDBCol;
    }

    public ColumnFamilyHandle getUrlIdDataRocksDBCol() {
        return urlIdDataRocksDBCol;
    }

    public ColumnFamilyHandle getWordIdDataRocksDBCol() {
        return wordIdDataRocksDBCol;
    }

    public List<ColumnFamilyHandle> getColumnFamilyHandleList() {
        return columnFamilyHandleList;
    }

    public SearchResultView getSiteSearchView(String url) throws RocksDBException {
        String siteId = RocksDBUtil.getUrlIdFromUrl(this.rocksDB, this.urlIdDataRocksDBCol, url);
        return getSiteSearchViewWithUrlId(siteId);
    }

    public SearchResultView getSiteSearchViewWithUrlId(String urlId) throws RocksDBException {
        // get meta data
        return null;
//        this.rocksDB.get(websiteMetaDataRocksDBCol, RocksDBColIndex.getIdToUrlKey(urlId).getBytes());
    }
}
