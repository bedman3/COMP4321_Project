package com.comp4321Project.searchEngine.Model;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Map;

public class InvertedFile {
    private final RocksDBDao rocksDBDao;
    private final ColumnFamilyHandle colHandle;
    private final HashMap<String, PostingList> hashMap;

    public InvertedFile(RocksDBDao rocksDBDao, ColumnFamilyHandle colHandle) {
        this.rocksDBDao = rocksDBDao;
        this.colHandle = colHandle;
        this.hashMap = new HashMap<String, PostingList>();
    }

    public void add(String wordId, String urlId, int location) throws RocksDBException {
        RocksDB rocksDB = this.rocksDBDao.getRocksDB();

        // check if the wordId exists in the hashMap
        PostingList postingList = this.hashMap.get(wordId);
        if (postingList == null) {
            // if posting list is not initialized in the in-memory hashmap, import posting list from rocksdb
            postingList = PostingList.fromBytesArray(rocksDB.get(colHandle, wordId.getBytes()));
        }
        postingList.add(urlId, location);
    }

    public void saveToRocksDB() throws RocksDBException {
        RocksDB rocksDB = this.rocksDBDao.getRocksDB();
        for (Map.Entry<String, PostingList> entry : this.hashMap.entrySet()) {
            rocksDB.put(this.colHandle, entry.getKey().getBytes(), entry.getValue().toBytesArray());
        }
    }
}
