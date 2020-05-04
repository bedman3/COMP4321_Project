//package com.comp4321Project.searchEngine;
//
//import com.comp4321Project.searchEngine.Dao.RocksDBDao;
//import com.comp4321Project.searchEngine.Service.BatchProcessing;
//import com.comp4321Project.searchEngine.Service.QuerySearch;
//import com.comp4321Project.searchEngine.Service.Spider;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.Test;
//import org.rocksdb.RocksDBException;
//
//import java.io.IOException;
//import java.util.Arrays;
//
//class MainTests {
//    @Test
//    public void crawlUrlToRocksDB() {
//        try {
//            String url = "http://www.cse.ust.hk";
//            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
//            Spider spider = new Spider(rocksDBDao);
//            spider.crawl(url, true, 30);
//            rocksDBDao.updateInvertedFileInRocksDB();
//        } catch (RocksDBException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void batchProcess() {
//        try {
//            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
//            BatchProcessing batchProcessing = BatchProcessing.getInstance(rocksDBDao);
//
//            batchProcessing.runBatchProcess();
//        } catch (RocksDBException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void printRocksDB() {
//        try {
//            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
//            rocksDBDao.printAllDataFromRocksDBToTextFile();
//        } catch (RocksDBException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void search() {
//        try {
//            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
//            QuerySearch querySearch = new QuerySearch(rocksDBDao);
//
//            String query;
//
//            query = "\"computer science\"";
//            System.out.println("Query: [" + query + "], Result: \n" + Arrays.toString(querySearch.search(query).getSearchResults().toArray()));
//
//            query = "visualization";
//            System.out.println("Query: [" + query + "], Result: \n" + Arrays.toString(querySearch.search(query).getSearchResults().toArray()));
//
//            query = "computer science";
//            System.out.println("Query: [" + query + "], Result: \n" + Arrays.toString(querySearch.search(query).getSearchResults().toArray()));
//
//            query = "artificial";
//            System.out.println("Query: [" + query + "], Result: \n" + Arrays.toString(querySearch.search(query).getSearchResults().toArray()));
//
//        } catch (RocksDBException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void computePageRank() {
//        try {
//            RocksDBDao rocksDBDao = RocksDBDao.getInstance();
//            BatchProcessing batchProcessing = BatchProcessing.getInstance(rocksDBDao);
//            batchProcessing.computePageRank();
//
//        } catch (RocksDBException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
