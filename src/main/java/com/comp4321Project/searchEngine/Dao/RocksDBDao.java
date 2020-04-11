package com.comp4321Project.searchEngine.Dao;

import com.comp4321Project.searchEngine.View.SearchResultView;

import java.util.Collection;
import java.util.HashMap;

public interface RocksDBDao {
    public Collection<SearchResultView> toList();
}
