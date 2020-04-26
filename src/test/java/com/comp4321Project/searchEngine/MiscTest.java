package com.comp4321Project.searchEngine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MiscTest {
    @Test
    public void testSth() {
        HashMap<Integer, List<Integer>> hashMap = new HashMap<Integer, List<Integer>>();
        List<Integer> linkedList = new LinkedList<Integer>();
        linkedList.add(1);
        hashMap.put(1, linkedList);

        hashMap.get(1).add(2);

        List<Integer> testList = hashMap.get(1);
        for (int i = 0; i < testList.size(); i++) {
            System.out.println(testList.get(i));
        }
    }
}
