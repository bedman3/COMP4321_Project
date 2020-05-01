package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Util.Util;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.util.HashMap;
import java.util.Map;

/**
 * This class will do:
 * 1) compute idf score
 * 2) compute pagerank
 */
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

    public void computePageRank() {

    }

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
        RocksIterator it = rocksDB.newIterator(rocksDBDao.getUrlIdToKeywordTermFrequencyRocksDBCol());
        HashMap<byte[], Double> idfScoreCache = new HashMap<>();

        for (it.seekToFirst(); it.isValid(); it.next()) {
            // for each doc vector
            HashMap<String, Double> tfIdfVector = new HashMap<>();
            // cast vector from byte array to hashmap
            HashMap<String, Double> tfVector = rocksDBDao.getKeywordTermFrequencyDataFromValue(it.value());
            for (Map.Entry<String, Double> entry : tfVector.entrySet()) {
                // compute tf idf score for each of the word inside the vector
                String wordId = entry.getKey();
                byte[] wordIdByte = wordId.getBytes();
                Double tfScore = entry.getValue();
                Double idfScore = idfScoreCache.get(wordIdByte);

                if (idfScore == null) {
                    // cache not exists
                    byte[] idfScoreByte = rocksDB.get(rocksDBDao.getWordIdToInverseDocumentFrequencyRocksDBCol(), wordIdByte);
                    idfScore = Double.parseDouble(new String(idfScoreByte));
                    idfScoreCache.put(wordIdByte, idfScore);
                }

                // compute tf idf score by multiplying them
                tfIdfVector.put(wordId, tfScore * idfScore);
            }

            rocksDBDao.putTfIdfScoreData(it.key(), tfIdfVector);
        }
        System.err.println("finished computing tfidf vector");
    }

    public void runBatchProcess() throws RocksDBException {
        computeIdfScoreForWord();
        computeTfIdfScoreForEachDocumentVector();
        computePageRank();
    }

}
