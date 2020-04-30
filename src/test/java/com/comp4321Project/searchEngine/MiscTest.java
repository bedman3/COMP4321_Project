package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MiscTest {
    @Test
    void printDB() {
        try {
            RocksDBDao rocksDBDao = new RocksDBDao();
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
        String timeString = "Wed, 29 Apr 2020 07:54:45 GMT";
        String timeString_1 = "Thu, 30 Apr 2020 07:54:45 GMT";
        ZonedDateTime zdt = ZonedDateTime.parse(timeString, DateTimeFormatter.RFC_1123_DATE_TIME);
        ZonedDateTime zdt_1 = ZonedDateTime.parse(timeString_1, DateTimeFormatter.RFC_1123_DATE_TIME);

        System.out.println(zdt.toString());
        System.out.println(zdt.compareTo(zdt_1));
        System.out.println(zdt_1.compareTo(zdt));
    }
}
