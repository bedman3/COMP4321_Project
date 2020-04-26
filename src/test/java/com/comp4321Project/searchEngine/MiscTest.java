package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Dao.RocksDBDaoImpl;
import com.comp4321Project.searchEngine.Model.PostingList;
import com.comp4321Project.searchEngine.Model.PostingNode;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;

import java.util.*;

public class MiscTest {
    @Test void printDB() {
        try {
            RocksDBDao rocksDBDao = new RocksDBDaoImpl();
            rocksDBDao.printAllDataInRocksDB();
        } catch (RocksDBException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSth1() {
        class TEST {
            void testFunc(List<Integer> list, Set<List<Integer>> set, Integer number) {
                list.add(number);
                set.add(list);
            }
        }

        Set<List<Integer>> hashSet = new HashSet<>();
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(1);
        TEST test = new TEST();
        test.testFunc(arrayList, hashSet, 2);
        test.testFunc(arrayList, hashSet, 3);

        Integer counter = 1;

        for (Iterator<List<Integer>> it = hashSet.iterator(); it.hasNext(); counter++) {
            List<Integer> printList = it.next();
            System.out.println("Print list: " + counter);
            for (int i = 0; i < printList.size(); i++) {
                System.out.println(printList.get(i));
            }
        }
    }

    @Test
    public void testSth2() {
         PostingList postingList = new PostingList();
        HashSet<PostingNode> hashSet = new HashSet<>();
        postingList.add("0", "1", 2, hashSet, true);
        postingList.add("0", "1", 3, hashSet, true);
        for (Iterator<PostingNode> it = hashSet.iterator(); it.hasNext(); ) {
            PostingNode node = it.next();
            List<Integer> intList = node.getLocationList();

            System.out.println("wordid: " + node.getWordId() + " urlid: " + node.getUrlId() + " hash: " + node.hashCode());
            for (int i = 0; i < intList.size(); i++) {
                System.out.println(intList.get(i));
            }
        }
    }
}
