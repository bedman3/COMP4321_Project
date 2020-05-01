package com.comp4321Project.searchEngine.Util;

import com.comp4321Project.searchEngine.Model.Constants;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Util {
    private static final Comparator<AbstractMap.SimpleEntry<String, Double>> tfIdfVectorComparator = new Comparator<AbstractMap.SimpleEntry<String, Double>>() {
        @Override
        public int compare(AbstractMap.SimpleEntry<String, Double> p1, AbstractMap.SimpleEntry<String, Double> p2) {
            return p1.getKey().compareTo(p2.getKey());
        }
    };

    public static void createDirectoryIfNotExist(String path) {
        Path currDir = Paths.get("");
        Path checkDir = Paths.get(currDir.toString(), path);
        File createDir = new File(checkDir.toString());
        if (createDir.mkdirs()) {
            System.err.println("created directory: " + checkDir.toAbsolutePath().toString());
        } else {
            System.err.println("directory " + checkDir.toAbsolutePath().toString() + " exists");
        }
    }

    public static void deleteRocksDBLockFile(String dbPath) {
        Path currDir = Paths.get("");
        Path checkDir = Paths.get(currDir.toString(), dbPath, "LOCK");
        File lockFile = new File(checkDir.toString());
        if (lockFile.exists()) {
            System.err.println("RocksDB LOCK file exists, try to remove it now");
            if (lockFile.delete()) {
                System.err.println("RocksDB LOCK file removed");
            } else {
                System.err.println("RocksDB LOCK file removal not successful");
            }
        } else {
            System.err.println("RocksDB LOCK file not found, proceed to next stage");
        }
    }

    public static double computeIdf(double totalNumOfDocuments, double totalNumOfDocWithTerm) {
        if (totalNumOfDocuments < 1 || totalNumOfDocWithTerm < 1) {
            return 0;
        }

        return Math.log(totalNumOfDocuments / totalNumOfDocWithTerm) / Constants.getLn2();
    }

    public static ArrayList<AbstractMap.SimpleEntry<String, Double>> transformTfIdfVector(HashMap<String, Double> tfIdfVector) {
        ArrayList<AbstractMap.SimpleEntry<String, Double>> arrayList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : tfIdfVector.entrySet()) {
            arrayList.add(new AbstractMap.SimpleEntry<String, Double>(entry.getKey(), entry.getValue()));
        }

        arrayList.sort(tfIdfVectorComparator);
        return arrayList;
    }

    public static ArrayList<AbstractMap.SimpleEntry<String, Double>> transformQueryIntoVector(List<String> queryWordList) {
        HashMap<String, Integer> keyFreqMap = new HashMap<>();
        ArrayList<AbstractMap.SimpleEntry<String, Double>> arrayList = new ArrayList<>();
        // count word frequency as a bag of word
        for (String wordId : queryWordList) {
            keyFreqMap.merge(wordId, 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : keyFreqMap.entrySet()) {
            arrayList.add(new AbstractMap.SimpleEntry<String, Double>(entry.getKey(), entry.getValue() * 1.0));
        }

        arrayList.sort(tfIdfVectorComparator);
        return arrayList;
    }

    /**
     * Compute Cos Sim Score given two lists are sorted in ascending mode and
     * in ArrayList<AbstractMap.SimpleEntry<String, Double>> format
     *
     * @param list1
     * @param list2
     * @return
     */
    public static Double computeCosSimScore(ArrayList<AbstractMap.SimpleEntry<String, Double>> list1, ArrayList<AbstractMap.SimpleEntry<String, Double>> list2) {
        if (list1.size() == 0 || list2.size() == 0) {
            return 0.0;
        }

        ArrayList<AbstractMap.SimpleEntry<String, Double>> longerList, shorterList;
        Double score = 0.0;
        if (list1.size() > list2.size()) {
            longerList = list1;
            shorterList = list2;
        } else {
            longerList = list2;
            shorterList = list1;
        }
        int longListLength = longerList.size();
        int shortListLength = shorterList.size();

        int longListPtr = 0;
        int shortListPtr = 0;

        while (longListPtr < longListLength && shortListPtr < shortListLength) {
            // no need to further check after reaching the end of one list
            int compare = longerList.get(longListPtr).getKey().compareTo(shorterList.get(shortListPtr).getKey());
            if (compare == 0) {
                score += longerList.get(longListPtr).getValue() * shorterList.get(shortListPtr).getValue();
                shortListPtr++;
                longListPtr++;
            } else if (compare < 0) {
                longListPtr++;
            } else { // compare > 0
                shortListPtr++;
            }
        }

        double longListSum = 0;
        double shortListSum = 0;

        for (int index = 0; index < longListLength; index++) {
            longListSum += longerList.get(index).getValue();
        }

        for (int index = 0; index < shortListLength; index++) {
            shortListSum += shorterList.get(index).getValue();
        }

        return score / (longListSum * shortListSum);
    }

}
