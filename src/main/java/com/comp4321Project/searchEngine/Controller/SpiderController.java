package com.comp4321Project.searchEngine.Controller;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Service.Spider;
import com.comp4321Project.searchEngine.View.Message;
import org.rocksdb.RocksDBException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/spider")
public class SpiderController {

    private final RocksDBDao rocksDBDao = new RocksDBDao();
    private final Spider spider = new Spider(rocksDBDao, Constants.getExtractTopKKeywords());

    public SpiderController() throws RocksDBException {
    }

    @RequestMapping(value = "/crawl", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Message spiderModel(@RequestParam(value = "url", defaultValue = "http://www.cse.ust.hk") String url,
                               @RequestParam(value = "recursive", defaultValue = "true") boolean recursive,
                               @RequestParam(value = "limit", defaultValue = "30") int limit) {
        try {
            spider.crawl(url, recursive, limit);

            rocksDBDao.getInvertedFileForBody().mergeExistingWithRocksDB();
            rocksDBDao.getInvertedFileForBody().flushToRocksDB();

            rocksDBDao.getInvertedFileForTitle().mergeExistingWithRocksDB();
            rocksDBDao.getInvertedFileForTitle().flushToRocksDB();

            return new Message(
                    "Crawling Complete",
                    null,
                    null
            );
        } catch (RocksDBException | IOException e) {
            System.err.println(e.toString());
            return new Message(
                    null,
                    null,
                    e.toString()
            );
        }
    }
}
