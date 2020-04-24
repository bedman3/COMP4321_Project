package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Dao.RocksDBDaoImpl;
import com.comp4321Project.searchEngine.Service.Spider;
import com.comp4321Project.searchEngine.Service.SpiderImpl;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

//	public static void main(String[] args) {
//		SpringApplication.run(Main.class, args);
//	}

    // my playground function for doing some RAD
    public static void main(String[] arg) throws IOException {
        final Integer extractTopKKeywords = 5;
        String url = "http://www.cse.ust.hk";

        try {
            RocksDBDao rocksDBDao = new RocksDBDaoImpl();

            Spider spider = new SpiderImpl(rocksDBDao, extractTopKKeywords);
            spider.crawl(url, true, 30);

            rocksDBDao.printAllDataInRocksDB();
        } catch (RocksDBException e) {
            System.err.println(e.toString());
        }
    }

}
