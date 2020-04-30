package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Service.Spider;
import org.junit.jupiter.api.BeforeAll;
import org.rocksdb.RocksDBException;

import java.io.IOException;

class MainTests {
    @BeforeAll
    public static void scrapeUrlToRocksDB() {
        try {
            String url = "http://www.cse.ust.hk";
            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
            Spider spider = new Spider(rocksDBDao, 5);
            spider.crawl(url, true, 30);

            rocksDBDao.getRocksDB().closeE();
        } catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }
    }

    /*@Test
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

            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
            QuerySearch querySearch = new QuerySearch(rocksDBDao);

            List<SiteMetaData> resultsList = querySearch.getAllSiteFromDB();
            resultsList.forEach(siteMetaData -> {
                printWriter.println(siteMetaData.toPrint());
                printWriter.println(dashedLineSeparator);
            });

            rocksDBDao.getRocksDB().closeE();
        } catch (RocksDBException | NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }*/

    // debug code
    /*@AfterAll
    public static void printAllDataInRocksDB() {
        try {
            RocksDBDao rocksDBDao = new RocksDBDaoImpl();
            rocksDBDao.printAllDataInRocksDB();
        } catch (RocksDBException | NullPointerException e) {
            e.printStackTrace();
        }
    }*/
}
