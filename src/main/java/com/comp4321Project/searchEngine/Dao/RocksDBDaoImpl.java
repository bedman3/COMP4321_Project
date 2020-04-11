package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.View.SearchResultView;

import java.util.Collection;
import java.util.HashMap;

public class RocksDBDaoImpl implements RocksDBDao {
    private HashMap<String, SearchResultView> searchResultViewHashMap;

    public RocksDBDaoImpl() {

    }

    public Collection<SearchResultView> toList() {
        return searchResultViewHashMap.values();
    }
}
