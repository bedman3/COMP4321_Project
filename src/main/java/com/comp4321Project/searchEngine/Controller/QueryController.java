package com.comp4321Project.searchEngine.Controller;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Service.QuerySearch;
import com.comp4321Project.searchEngine.View.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
public class QueryController {
    @Autowired
    private final RocksDBDao rocksDBDao;
    private final QuerySearch querySearch;

    public QueryController(RocksDBDao rocksDBDao, QuerySearch querySearch) {
        this.rocksDBDao = rocksDBDao;
        this.querySearch = querySearch;
    }

    @ExceptionHandler(Exception.class)
    public Message error(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        return new Message(null, "error", ExceptionUtils.getStackTrace(e));
    }

    @CrossOrigin
    @RequestMapping(value = "/get-all-result", method = RequestMethod.GET)
    public List<SearchResultsView> getAllResult() throws RocksDBException {
        return querySearch.getAllSiteFromDB();
    }

    @CrossOrigin
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public QuerySearchResponseView search(@RequestBody SearchRequest searchRequest) throws RocksDBException {
        QuerySearchResponseView queryResponse = querySearch.search(searchRequest.query);
        rocksDBDao.putQueryHistory(searchRequest.query, queryResponse);
        return queryResponse;
    }

    @CrossOrigin
    @RequestMapping(value = "/stemmed-keywords", method = RequestMethod.GET)
    public StemmedKeywordsView stemmedKeywords() throws RocksDBException {
        return querySearch.getAllStemmedKeywords();
    }

    @CrossOrigin
    @RequestMapping(value = "/suggestions", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<String> suggestions(@RequestBody SearchRequest searchRequest) {
        System.err.println(searchRequest.query);

        ArrayList<String> arrayList = new ArrayList<>(10);
        for (Integer i = 0; i < 10; i++) {
            arrayList.add(searchRequest.query + i.toString());
        }

        return arrayList;
    }

    @CrossOrigin
    @RequestMapping(value = "/query-history", method = RequestMethod.GET)
    public List<QueryHistoryView> queryHistory() throws RocksDBException {
        return querySearch.getQueryHistory(Constants.getDefaultNumOfQueryHistory());
    }

    static class SearchRequest {
        String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        @Override
        public String toString() {
            return "SearchRequest{" +
                    "query='" + query + '\'' +
                    '}';
        }
    }
}
