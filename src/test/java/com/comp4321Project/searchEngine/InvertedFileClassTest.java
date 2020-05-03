package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Model.PostingList;
import com.comp4321Project.searchEngine.Model.PostingNode;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class InvertedFileClassTest {
    private static final String rocksDBTestFilePath = "rocksDBFiles_Test";

    @BeforeAll
    public static void setUpTestRocksDB() throws RocksDBException {
        RocksDBDao.getInstance(rocksDBTestFilePath);
    }

    @AfterAll
    public static void removeTestRocksDBFiles() throws IOException {
        File file = new File(rocksDBTestFilePath);
        FileUtils.deleteDirectory(file);
    }

    @Test
    public void testAddInvertedFile() throws RocksDBException {
        RocksDBDao rocksDBDao = RocksDBDao.getInstance(rocksDBTestFilePath);
        InvertedFile invertedFile = new InvertedFile(rocksDBDao, rocksDBDao.getInvertedFileForBodyWordIdToPostingListRocksDBCol());

        invertedFile.add("0", "0", 0, true);
        invertedFile.add("0", "0", 1, true);
        invertedFile.add("0", "1", 1, true);
        invertedFile.add("1", "0", 1, true);
        invertedFile.add("1", "0", 0, true);
        invertedFile.add("1", "1", 1, true);

        invertedFile.flushToRocksDB();

        PostingList pList = invertedFile.getPostingListWithWordId("0");
        Assertions.assertEquals(2, pList.size());

        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("0");
        hashSet.add("1");
        Assertions.assertEquals(hashSet, pList.getAllUrlIdFromPostingList());

        PostingNode pNode = pList.search("0");
        Assertions.assertEquals("0", pNode.getUrlId());
        Assertions.assertEquals(0, pNode.getUrlIdInteger());
        List<Integer> lList = pNode.getLocationList();
        Assertions.assertEquals(2, lList.size());
        Assertions.assertEquals(0, lList.get(0));
        Assertions.assertEquals(1, lList.get(1));

        invertedFile.add("0", "0", 0, true);
        invertedFile.add("0", "0", 1, true);
        invertedFile.add("0", "1", 1, true);
        invertedFile.add("1", "0", 1, true);
        invertedFile.add("1", "0", 0, true);
        invertedFile.add("1", "1", 1, true);

        invertedFile.mergeExistingWithRocksDB();
        invertedFile.flushToRocksDB();

        pList = invertedFile.getPostingListWithWordId("0");
        Assertions.assertEquals(2, pList.size());

        hashSet = new HashSet<>();
        hashSet.add("0");
        hashSet.add("1");
        Assertions.assertEquals(hashSet, pList.getAllUrlIdFromPostingList());

        pNode = pList.search("0");
        Assertions.assertEquals("0", pNode.getUrlId());
        Assertions.assertEquals(0, pNode.getUrlIdInteger());
        lList = pNode.getLocationList();
        Assertions.assertEquals(2, lList.size());
        Assertions.assertEquals(0, lList.get(0));
        Assertions.assertEquals(1, lList.get(1));
    }
}
