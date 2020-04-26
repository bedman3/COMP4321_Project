package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.View.SiteMetaData;
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

    public ColumnFamilyHandle getInvertedFileForBodyWordIdToPostingList();

    public ColumnFamilyHandle getInvertedFileForTitleWordIdToPostingList();

    public List<ColumnFamilyHandle> getColumnFamilyHandleList();

    public SiteMetaData getSiteSearchViewWithUrlId(String urlId) throws RocksDBException;

    public void printAllDataInRocksDB() throws RocksDBException;

    public void closeRocksDB() throws RocksDBException;

    public void initRocksDBWithNextAvailableId(ColumnFamilyHandle colHandle) throws RocksDBException;

    public String getUrlIdFromUrl(String url) throws RocksDBException;

    public String getWordIdFromWord(String word) throws RocksDBException;

    public String getUrlFromUrlId(String urlId) throws RocksDBException;

    public String getWordFromWordId(String wordId) throws RocksDBException;

    public String getIdFromKey(ColumnFamilyHandle keyToIdColHandle, ColumnFamilyHandle idToKeyColHandle, String key) throws RocksDBException;

    public String getKeyFromId(ColumnFamilyHandle idToKeyColHandle, String id) throws RocksDBException;

}
