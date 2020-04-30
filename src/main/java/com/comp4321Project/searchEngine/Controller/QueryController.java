package com.comp4321Project.searchEngine.Controller;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Service.QuerySearch;
import com.comp4321Project.searchEngine.View.Message;
import com.comp4321Project.searchEngine.View.SearchResultsView;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.rocksdb.RocksDBException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class QueryController {
    private final RocksDBDao rocksDBDao;
    private final QuerySearch querySearch;

    public QueryController() throws RocksDBException {
        this.rocksDBDao = RocksDBDao.getInstance();
        this.querySearch = new QuerySearch(rocksDBDao);
    }

    @ExceptionHandler(Exception.class)
    public Message error(HttpServletRequest request, Exception e) {
        return new Message(null, "error", ExceptionUtils.getStackTrace(e));
    }

    @RequestMapping(value = "/get-all-result", method = RequestMethod.GET)
    public List<SearchResultsView> getAllResult() throws RocksDBException {
        return querySearch.getAllSiteFromDB();
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void search(@RequestParam(value = "query") String query) {
        try {
            querySearch.search(query);
        } catch (RocksDBException e) {
            System.err.println(e.toString());

        }
    }
}
