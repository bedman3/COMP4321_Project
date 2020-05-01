package com.comp4321Project.searchEngine.Controller;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Service.Spider;
import com.comp4321Project.searchEngine.View.Message;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@EnableAsync
@RestController
public class SpiderController {

    @Autowired
    private final RocksDBDao rocksDBDao;
    private final Spider spider;

    public SpiderController(RocksDBDao rocksDBDao, Spider spider) throws RocksDBException {
        this.rocksDBDao = rocksDBDao;
        this.spider = spider;
    }

    @ExceptionHandler(Exception.class)
    public Message error(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        return new Message(null, "error", ExceptionUtils.getStackTrace(e));
    }

    static class CrawlRequest {
        String url;
        Boolean recursive;
        Integer limit;

        public CrawlRequest() {
            this.url = "http://www.cse.ust.hk";
            this.recursive = true;
            this.limit = 30;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Boolean getRecursive() {
            return recursive;
        }

        public void setRecursive(Boolean recursive) {
            this.recursive = recursive;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }
    }

    @Async
    @RequestMapping(value = "/crawl", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Message spiderModel(@RequestBody CrawlRequest crawlRequest) throws IOException, RocksDBException {
        spider.crawl(crawlRequest.url, crawlRequest.recursive, crawlRequest.limit);
        return new Message(
                "Crawl request received",
                null,
                null
        );
    }
}
