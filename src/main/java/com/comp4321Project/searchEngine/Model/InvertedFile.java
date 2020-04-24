package com.comp4321Project.searchEngine.Model;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.HashMap;

public class InvertedFile {
    private final RocksDBDao rocksDBDao;
    private final ColumnFamilyHandle colHandle;

    public InvertedFile(RocksDBDao rocksDBDao, ColumnFamilyHandle colHandle) {
        this.rocksDBDao = rocksDBDao;
        this.colHandle = colHandle;
    }

    public void add(String wordId, String urlId, int location) throws RocksDBException {
        RocksDB rocksDB = this.rocksDBDao.getRocksDB();

        // check if the wordId exists in the table
        byte[] postingListByte = rocksDB.get(colHandle, wordId.getBytes());
        if (postingListByte == null) {
            PostingList postingList = new PostingList();
            postingList.add(urlId, location);

            rocksDB.put(colHandle, wordId.getBytes(), postingList.toBytesArray());
        } else {

        }
    }
}
