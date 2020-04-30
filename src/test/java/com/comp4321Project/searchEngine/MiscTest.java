package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;

import java.util.HashSet;

public class MiscTest {
    @Test
    public void test() throws RocksDBException {
        RocksDBDao rocksDBDao = RocksDBDao.getInstance();
        HashSet<byte[]> hashSet = new HashSet<>();

        byte[] testByte = "Testing".getBytes();

        hashSet.add("Testing".getBytes());
        hashSet.add(testByte);

        assert hashSet.size() == 2;
    }
}
