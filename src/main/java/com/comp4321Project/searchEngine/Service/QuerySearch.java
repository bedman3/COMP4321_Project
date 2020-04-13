package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Util.RocksDBUtil;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.rocksdb.RocksDBException;

public class QuerySearch {
    RocksDBDao rocksDBDao;

    public QuerySearch(RocksDBDao rocksDBDao) {
       this.rocksDBDao = rocksDBDao;
    }

    public SiteMetaData search(String url) throws RocksDBException {
        String urlId = RocksDBUtil.getUrlIdFromUrl(rocksDBDao, url);

        SiteMetaData siteMetaData = rocksDBDao.getSiteSearchViewWithUrlId(urlId);
        siteMetaData.updateParentLinks(rocksDBDao, urlId);

        return siteMetaData;
    }
}
