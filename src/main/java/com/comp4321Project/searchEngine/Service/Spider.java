package com.comp4321Project.searchEngine.Service;

import org.rocksdb.RocksDBException;

import java.io.IOException;

public interface Spider {
    public void scrape(String url) throws IOException, RocksDBException;

    public void scrape(String url, Boolean recursive) throws IOException, RocksDBException;

    public void scrape(String url, Boolean recursive, Integer limit) throws IOException, RocksDBException;
}
