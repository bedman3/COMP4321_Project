//package com.comp4321Project.searchEngine.Controller;
//
//import com.comp4321Project.searchEngine.Dao.RocksDBDao;
//import com.comp4321Project.searchEngine.Service.Spider;
//import com.comp4321Project.searchEngine.View.SearchResultView;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/spider")
//public class SpiderController {
//
//    @Autowired
//    private Spider spider;
//
//    @RequestMapping(value = "/scrape-website", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public List<SearchResultView> spiderModel(@RequestParam(value = "url", defaultValue = "http://www.cse.ust.hk") String url) {
//        RocksDBDao rocksDBDao = new RocksDBDao();
//
//        Spider spider = new Spider(url, rocksDBDao);
//        spider.scrape();
//
//        return rocksDBDao.toList();
//    }
//}
