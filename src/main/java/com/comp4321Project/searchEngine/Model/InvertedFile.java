package com.comp4321Project.searchEngine.Model;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class InvertedFile {
    private final RocksDBDao rocksDBDao;
    private final ColumnFamilyHandle colHandle;
    private final HashMap<String, PostingList> hashMap;
    private final HashSet<PostingNode> lazySortNodeSet;

    public InvertedFile(RocksDBDao rocksDBDao, ColumnFamilyHandle colHandle) {
        this.rocksDBDao = rocksDBDao;
        this.colHandle = colHandle;
        this.hashMap = new HashMap<String, PostingList>();
        this.lazySortNodeSet = new HashSet<>();
    }

    public void add(String wordId, String urlId, int location, boolean lazy) throws RocksDBException {
        // check if the wordId exists in the hashMap
        PostingList postingList = this.hashMap.get(wordId);
        if (postingList == null) {
            // if posting list is not initialized in the in-memory hashmap, import posting list from rocksdb
            postingList = PostingList.fromBytesArray(this.rocksDBDao.getRocksDB().get(colHandle, wordId.getBytes()));

        }
        postingList.add(wordId, urlId, location, lazySortNodeSet, lazy);
        this.hashMap.put(wordId, postingList);
    }

    /**
     * this function will do:
     * 1) scan the posting list required to update
     * 2) import posting list from rocksdb
     * 3) if there does not exist posting list for keyword, skip merging and use the posting list from hash map
     * 4) if there exists posting list for keyword, import it to memory, merge it with the existing list
     * by default will do the sorting for the location list in posting list
     */
    public void mergeExistingWithRocksDB() throws RocksDBException {
        for (PostingNode node : lazySortNodeSet) {
            // sort the order of the locations in doc list
            node.sort();
        }
        // clear the set after sorting
        lazySortNodeSet.clear();

        for (Map.Entry<String, PostingList> entry : hashMap.entrySet()) {
            PostingList postingList;
            byte[] postingListByteFromRocksDB = rocksDBDao.getRocksDB().get(colHandle, entry.getKey().getBytes());
            if (postingListByteFromRocksDB == null) {
                // if posting list does not exist in rocksdb
                // do nothing here, no need to update the in memory posting list
            } else {
                // read the posting list
                postingList = PostingList.fromBytesArray(postingListByteFromRocksDB);

                // merge the in memory posting list into the existing posting list
                postingList.merge(entry.getValue());
                hashMap.put(entry.getKey(), postingList);
            }
        }
    }

    public void flushToRocksDB() throws RocksDBException {
        RocksDB rocksDB = this.rocksDBDao.getRocksDB();
        for (Map.Entry<String, PostingList> entry : this.hashMap.entrySet()) {
            rocksDB.put(this.colHandle, entry.getKey().getBytes(), entry.getValue().toBytesArray());
            // record the total number of docs this word appear in
            if (this.colHandle == rocksDBDao.getInvertedFileForBodyWordIdToPostingListRocksDBCol()) {
                rocksDB.put(rocksDBDao.getWordIdToDocumentFrequencyRocksDBCol(), entry.getKey().getBytes(), entry.getValue().size().toString().getBytes());
            }
        }
        this.hashMap.clear();
    }
}
