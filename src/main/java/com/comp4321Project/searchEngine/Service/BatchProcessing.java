package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;

public class BatchProcessing {
    public static BatchProcessing getInstance(RocksDBDao rocksDBDao) {
        if (instance == null && BatchProcessing.rocksDBDao == null && rocksDBDao != null) {
            instance = new BatchProcessing(rocksDBDao);
        }
        return instance;
    }

    public static BatchProcessing getInstance() {
        return getInstance(rocksDBDao);
    }

    private static BatchProcessing instance;
    private static RocksDBDao rocksDBDao;

    private BatchProcessing(RocksDBDao rocksDBDao) {
        BatchProcessing.rocksDBDao = rocksDBDao;
    }


}
