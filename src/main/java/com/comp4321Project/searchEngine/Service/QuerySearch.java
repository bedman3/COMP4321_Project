package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.util.ArrayList;
import java.util.List;

public class QuerySearch {
    RocksDBDao rocksDBDao;

    public QuerySearch(RocksDBDao rocksDBDao) {
        this.rocksDBDao = rocksDBDao;
    }

    public SiteMetaData search(String query) throws RocksDBException {
        String processedQuery

//        String urlId = rocksDBDao.getUrlIdFromUrl(url);
//
//        SiteMetaData siteMetaData = rocksDBDao.getSiteSearchViewWithUrlId(urlId);
//        siteMetaData.updateParentLinks(rocksDBDao, urlId);
//
//        return siteMetaData;
    }

    public List<SiteMetaData> getAllSiteFromDB() throws RocksDBException {
        // search from meta data and extract all scraped site

        RocksDB rocksDB = rocksDBDao.getRocksDB();
        RocksIterator it = rocksDB.newIterator(rocksDBDao.getUrlIdToMetaDataRocksDBCol());
        List<SiteMetaData> returnList = new ArrayList<>();

        for (it.seekToFirst(); it.isValid(); it.next()) {
            String urlId = new String(it.key());
            returnList.add(rocksDBDao.getSiteSearchViewWithUrlId(urlId).updateParentLinks(rocksDBDao, urlId));
        }

        return returnList;
    }
}
