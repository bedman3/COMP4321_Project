package com.comp4321Project.searchEngine.Service;

import org.rocksdb.RocksDBException;

import java.io.IOException;

public interface Spider {
    public void crawl(String url) throws IOException, RocksDBException;

    public void crawl(String url, Boolean recursive) throws IOException, RocksDBException;

    public void crawl(String url, Boolean recursive, Integer limit) throws IOException, RocksDBException;
}
