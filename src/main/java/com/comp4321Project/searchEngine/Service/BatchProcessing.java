package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;
import com.comp4321Project.searchEngine.Util.Util;
import com.google.gson.Gson;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class PageRankRaw implements Serializable {
    int maxDoc;
    HashMap<Integer, List<Integer>> pageRankDict;
    int pageRankIteration;

    public PageRankRaw(int maxDoc, HashMap<Integer, List<Integer>> pageRankDict, int pageRankIteration) {
        this.maxDoc = maxDoc;
        this.pageRankDict = pageRankDict;
        this.pageRankIteration = pageRankIteration;
    }
}

class PageRankResult implements Serializable {
    List<Double> result;
}

/**
 * This class will do:
 * 1) compute idf score
 * 2) compute pagerank
 */
@Service
public class BatchProcessing {
    private static BatchProcessing instance;
    private static RocksDBDao rocksDBDao;

    private BatchProcessing(RocksDBDao rocksDBDao) {
        BatchProcessing.rocksDBDao = rocksDBDao;
    }

    public static BatchProcessing getInstance(RocksDBDao rocksDBDao) {
        if (instance == null && BatchProcessing.rocksDBDao == null && rocksDBDao != null) {
            instance = new BatchProcessing(rocksDBDao);
        }
        return instance;
    }

    public static BatchProcessing getInstance() {
        return getInstance(rocksDBDao);
    }

    public void computePageRank() throws RocksDBException, IOException {
        System.err.println("start computing page rank");
        Gson gson = new Gson();

        // prepare pagerank_raw.json
        {
            Integer totalNumOfDocument = rocksDBDao.getTotalNumOfDocuments();

            HashMap<Integer, List<Integer>> dict = new HashMap<>();
            RocksIterator it = rocksDBDao.getRocksDB().newIterator(rocksDBDao.getParentUrlIdToChildUrlIdRocksDBCol());
            for (it.seekToFirst(); it.isValid(); it.next()) {
                Integer parentId = Integer.parseInt(new String(it.key()));
                dict.put(parentId, ((HashSet<String>) CustomFSTSerialization.getInstance().asObject(it.value()))
                        .stream()
                        .map(Integer::parseInt)
                        .collect(Collectors.toCollection(ArrayList::new)));

            }
            File outputFile = new File("pagerank_raw.json");
            outputFile.delete();
            outputFile.createNewFile();
            PrintWriter printWriter = new PrintWriter(outputFile);
            printWriter.print(gson.toJson(new PageRankRaw(totalNumOfDocument, dict, Constants.getPageRankIteration())));
            printWriter.close();
        }

        // setup virtual env for python and download required python library, numpy and scipy
        {
//            Process process = new ProcessBuilder("./venv/bin/python", "PageRank.py").redirectErrorStream(true).start();
            Process process = new ProcessBuilder("python", "-m", "venv", "venv").redirectErrorStream(true).start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            System.err.println("creating virtual env for python");
            String line;
            while ((line = br.readLine()) != null) {
                System.err.println(line);
            }

            process = new ProcessBuilder("./venv/bin/pip", "install", "numpy", "scipy").redirectErrorStream(true).start();
            is = process.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            System.err.println("downloading library for python");
            while ((line = br.readLine()) != null) {
                System.err.println(line);
            }
        }

        // compute page rank in python script
        {
            Process process = new ProcessBuilder("./venv/bin/python", "./PageRank.py").start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                System.err.println(line);
            }
        }

        {
            File file = new File("pagerank_results.json");
            Scanner scanner = new Scanner(file);


            PageRankResult pageRankResult = gson.fromJson(new String(Files.readAllBytes(Paths.get("pagerank_results.json"))), PageRankResult.class);
            List<Double> pageRankScoreList = pageRankResult.result;
            RocksDB rocksDB = rocksDBDao.getRocksDB();
            ColumnFamilyHandle colHandle = rocksDBDao.getUrlIdToPageRankScoreRocksDBCol();
            for (Integer index = 0; index < pageRankScoreList.size(); index++) {
                rocksDB.put(colHandle, index.toString().getBytes(), pageRankScoreList.get(index).toString().getBytes());
            }
        }

        System.err.println("finished computing page rank");
    }

//    public void computePageRank() throws RocksDBException {
//        System.err.println("start computing page rank");
//        Integer totalNumOfDocument = rocksDBDao.getTotalNumOfDocuments();
//
//
//        INDArray weightMatrix = Nd4j.zeros(totalNumOfDocument, totalNumOfDocument);
//        INDArray docVector = Nd4j.ones(1, totalNumOfDocument);
//        INDArray dampingVector = docVector.mul(1 - Constants.getDampingFactor());
//
//        // prepare sparse matrix
//        {
//            RocksIterator it = rocksDBDao.getRocksDB().newIterator(rocksDBDao.getParentUrlIdToChildUrlIdRocksDBCol());
//            for (it.seekToFirst(); it.isValid(); it.next()) {
//                Integer parentId = Integer.parseInt(new String(it.key()));
//                ArrayList<Integer> childIdsList = ((HashSet<String>) CustomFSTSerialization.getInstance().asObject(it.value()))
//                        .stream()
//                        .map(Integer::parseInt)
//                        .collect(Collectors.toCollection(ArrayList::new));
//
//                Integer totalChild = childIdsList.size();
//                if (totalChild == 0) continue;
//                Double assignValue = 1.0 / totalChild;
//
//                childIdsList.forEach(index -> weightMatrix.putScalar(new int[]{parentId, index}, assignValue));
//            }
//        }
//
//        System.err.println(weightMatrix.toString());
//
//        // compute page rank score
//        for (int iteration = 0; iteration < Constants.getPageRankIteration(); iteration++) {
//            System.err.println(iteration);
//            docVector = docVector.mul(Constants.getDampingFactor()).mul(weightMatrix).add(dampingVector);
//        }
//
//        System.err.println(docVector.toString());
//
//        System.err.println("finished computing page rank");
//    }

    public void computeIdfScoreForWord() throws RocksDBException {
        System.err.println("start computing idf score for word");
        double totalNumOfDocuments = rocksDBDao.getTotalNumOfDocuments() * 1.0;

        if (totalNumOfDocuments < 1) {
            // skip processing if there is no document in the database
            System.err.println("totalNumOfDocuments < 1, skip computing idf score for word");
            return;
        } else {
            RocksDB rocksDB = rocksDBDao.getRocksDB();
            RocksIterator it = rocksDB.newIterator(rocksDBDao.getWordIdToDocumentFrequencyRocksDBCol());

            for (it.seekToFirst(); it.isValid(); it.next()) {
                Double idf = Util.computeIdf(totalNumOfDocuments, Double.parseDouble(new String(it.value())));
                rocksDB.put(rocksDBDao.getWordIdToInverseDocumentFrequencyRocksDBCol(), it.key(), idf.toString().getBytes());
            }
            System.err.println("finished computing idf score for word");
        }
    }

    public void computeTfIdfScoreForEachDocumentVector() throws RocksDBException {
        System.err.println("start computing tfidf vector");

        RocksDB rocksDB = rocksDBDao.getRocksDB();
        RocksIterator it = rocksDB.newIterator(rocksDBDao.getUrlIdToKeywordTermFrequencyForBodyRocksDBCol());
        HashMap<String, Double> idfScoreCache = new HashMap<>();

        for (it.seekToFirst(); it.isValid(); it.next()) {
            // for each doc vector
            ArrayList<ImmutablePair<String, Double>> tfIdfVector = new ArrayList<>();
            // cast vector from byte array to hashmap
            HashMap<String, Double> tfVector = rocksDBDao.getKeywordTermFrequencyDataForBodyFromValue(it.value());
            for (Map.Entry<String, Double> entry : tfVector.entrySet()) {
                // compute tf idf score for each of the word inside the vector
                String wordId = entry.getKey();
                byte[] wordIdByte = wordId.getBytes();
                Double tfScore = entry.getValue();
                Double idfScore = idfScoreCache.get(wordId);

                if (idfScore == null) {
                    // cache not exists
                    byte[] idfScoreByte = rocksDB.get(rocksDBDao.getWordIdToInverseDocumentFrequencyRocksDBCol(), wordIdByte);
                    try {
                        idfScore = Double.parseDouble(new String(idfScoreByte));
                    } catch (NullPointerException e) {
                        System.err.println("null pointer, idfScore: " + idfScoreByte + " wordId: " + new String(wordIdByte));
                        idfScore = 0.0;
                    }
                    idfScoreCache.put(wordId, idfScore);
                }

                // compute tf idf score by multiplying them
                tfIdfVector.add(new ImmutablePair<>(wordId, tfScore * idfScore));
            }

            tfIdfVector.sort(Util.getTfIdfVectorComparator());
            rocksDBDao.putTfIdfScoreData(it.key(), tfIdfVector);
        }
        System.err.println("finished computing tfidf vector");
    }

    public void runBatchProcess() throws RocksDBException, IOException {
        resetQueryResponseCache();
        resetMiscellaneousCache();
        computeIdfScoreForWord();
        computeTfIdfScoreForEachDocumentVector();
        computeTitleVectorForEachDocument();
        computePageRank();
        computeMiscellaneousCache();
    }

    public ArrayList<String> computeStemmedKeywordsList() throws RocksDBException {
        System.err.println("start computing stemmed keywords cache");
        RocksIterator it = rocksDBDao.getRocksDB().newIterator(rocksDBDao.getWordToWordIdRocksDBCol());

        LinkedList<String> linkedList = new LinkedList<>();

        for (it.seekToFirst(); it.isValid(); it.next()) {
            linkedList.addLast(new String(it.key()));
        }
        ArrayList<String> arrayList = new ArrayList<>(linkedList);
        rocksDBDao.putStemmedKeywordCache(arrayList);

        System.err.println("finished computing stemmed keywords cache");
        return arrayList;
    }

    private void computeMiscellaneousCache() throws RocksDBException {
        System.err.println("start computing miscellaneous cache");
        computeStemmedKeywordsList();
        System.err.println("finished computing miscellaneous cache");
    }

    private void resetMiscellaneousCache() throws RocksDBException {
        System.err.println("start resetting miscellaneous cache");
        rocksDBDao.resetMiscellaneousCache();
        System.err.println("finished resetting miscellaneous cache");
    }

    private void resetQueryResponseCache() throws RocksDBException {
        System.err.println("start resetting query response cache");
        rocksDBDao.resetQueryResponseCache();
        System.err.println("finished resetting query response cache");
    }

    private void computeTitleVectorForEachDocument() throws RocksDBException {
        System.err.println("start computing title vector");

        RocksDB rocksDB = rocksDBDao.getRocksDB();
        RocksIterator it = rocksDB.newIterator(rocksDBDao.getUrlIdToKeywordFrequencyForTitleRocksDBCol());

        for (it.seekToFirst(); it.isValid(); it.next()) {
            HashMap<String, Integer> keyFreqForTitleMap = rocksDBDao.getKeywordFrequencyDataForTitleFromByte(it.key());
            ArrayList<ImmutablePair<String, Double>> keywordFrequencyVector = Util.transformKeywordFrequencyMapToVector(keyFreqForTitleMap);
            rocksDBDao.putKeywordVectorForTitle(it.key(), keywordFrequencyVector);
        }

        System.err.println("finished computing title vector");
    }

}
