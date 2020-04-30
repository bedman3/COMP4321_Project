package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Service.QuerySearch;
import com.comp4321Project.searchEngine.Service.Spider;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.Arrays;

class MainTests {
    @Test
    public void scrapeUrlToRocksDB() {
        try {
            String url = "http://www.cse.ust.hk";
            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
            Spider spider = new Spider(rocksDBDao, 5);
            spider.crawl(url, true, 30);
            rocksDBDao.updateInvertedFileInRocksDB();
        } catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void printRocksDB() {
        try {
            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
            rocksDBDao.printAllDataInRocksDB();
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void search() {
        try {
            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
            QuerySearch querySearch = new QuerySearch(rocksDBDao);

            String query;

//            query = "suck";
//            System.out.println("Query: [" + query + "], Result: \n" + Arrays.toString(querySearch.search(query).toArray()));

            query = "visualization";
            System.out.println("Query: [" + query + "], Result: \n" + Arrays.toString(querySearch.search(query).toArray()));

            query = "computer science";
            System.out.println("Query: [" + query + "], Result: \n" + Arrays.toString(querySearch.search(query).toArray()));

            query = "artificial";
            System.out.println("Query: [" + query + "], Result: \n" + Arrays.toString(querySearch.search(query).toArray()));

        } catch (RocksDBException e) {
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
