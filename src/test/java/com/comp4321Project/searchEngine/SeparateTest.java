package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Dao.RocksDBDaoImpl;
import com.comp4321Project.searchEngine.Service.QuerySearch;
import com.comp4321Project.searchEngine.Service.Spider;
import com.comp4321Project.searchEngine.Service.SpiderImpl;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SeparateTest {
    @Test
    public void scrapeUrlToRocksDB() {
        try {
            String url = "http://www.cse.ust.hk";
            RocksDBDao rocksDBDao = new RocksDBDaoImpl();
            Spider spider = new SpiderImpl(rocksDBDao, 5);
            spider.scrape(url, true, 30);

            rocksDBDao.getRocksDB().closeE();
        } catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadResultFromRocksDB() {
        try {
            String outputFileName = "spider_result.txt";
            String dashedLineSeparator = "--------------------------------------------------------------------------";
            File outputFile = new File(outputFileName);

            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            PrintWriter printWriter = new PrintWriter(outputFile);

            RocksDBDao rocksDBDao = new RocksDBDaoImpl();
            QuerySearch querySearch = new QuerySearch(rocksDBDao);

            List<SiteMetaData> resultsList = querySearch.getAllSiteFromDB();
            resultsList.forEach(siteMetaData -> System.out.println(siteMetaData.toPrint()));
            resultsList.forEach(siteMetaData -> {
                printWriter.println(siteMetaData.toPrint());
                printWriter.println(dashedLineSeparator);
            });

            rocksDBDao.getRocksDB().closeE();
        } catch (RocksDBException | NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }
}
