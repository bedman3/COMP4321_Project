package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Dao.RocksDBDaoImpl;
import com.comp4321Project.searchEngine.Service.QuerySearch;
import com.comp4321Project.searchEngine.Service.Spider;
import com.comp4321Project.searchEngine.Service.SpiderImpl;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class MainTests {
    @BeforeAll
    static void scrapeUrlToRocksDB() {
        try {
            String url = "http://www.cse.ust.hk";
            RocksDBDao rocksDBDao = new RocksDBDaoImpl();
            Spider spider = new SpiderImpl(rocksDBDao, 5);
            spider.scrape(url, true, 30);
        } catch (RocksDBException | IOException e) {
            System.err.println(e.toString());
        }
    }

    @Test
    void loadResultFromRocksDB() {
        try {
            String url = "http://www.cse.ust.hk";
            RocksDBDao rocksDBDao = new RocksDBDaoImpl();
            QuerySearch querySearch = new QuerySearch(rocksDBDao);

            SiteMetaData siteMetaData = querySearch.search(url);
            System.out.println(siteMetaData.toPrint());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}
