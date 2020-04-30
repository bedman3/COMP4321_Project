package com.comp4321Project.searchEngine.Controller;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Service.QuerySearch;
import org.rocksdb.RocksDBException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class QueryController {
    private final RocksDBDao rocksDBDao = new RocksDBDao();
    private final QuerySearch querySearch = new QuerySearch(rocksDBDao);

    public QueryController() throws RocksDBException {

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
