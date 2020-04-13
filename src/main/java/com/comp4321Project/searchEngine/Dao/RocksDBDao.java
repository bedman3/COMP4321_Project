package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.View.SearchResultView;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.List;

public interface RocksDBDao {
    public RocksDB getRocksDB();

    public ColumnFamilyHandle getDefaultRocksDBCol();

    public ColumnFamilyHandle getUrlIdToMetaDataRocksDBCol();
    public ColumnFamilyHandle getParentUrlIdToChildUrlIdRocksDBCol();
    public ColumnFamilyHandle getChildUrlIdToParentUrlIdRocksDBCol();
    public ColumnFamilyHandle getUrlToUrlIdRocksDBCol();
    public ColumnFamilyHandle getUrlIdToUrlRocksDBCol();
    public ColumnFamilyHandle getWordToWordIdRocksDBCol();
    public ColumnFamilyHandle getWordIdToWordRocksDBCol();
    public ColumnFamilyHandle getUrlIdToKeywordFrequencyRocksDBCol();
    public ColumnFamilyHandle getUrlIdToTop5Keyword();

    public List<ColumnFamilyHandle> getColumnFamilyHandleList();

    public SearchResultView getSiteSearchView(String url) throws RocksDBException;

    public SearchResultView getSiteSearchViewWithUrlId(String urlId) throws RocksDBException;
}
