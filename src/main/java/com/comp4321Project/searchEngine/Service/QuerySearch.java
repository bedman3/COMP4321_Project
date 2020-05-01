package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Util.TextProcessing;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.SearchResultsView;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.util.*;
import java.util.stream.Collectors;

public class QuerySearch {
    RocksDBDao rocksDBDao;

    public QuerySearch(RocksDBDao rocksDBDao) {
        this.rocksDBDao = rocksDBDao;
    }

    public List<SearchResultsView> search(String query) throws RocksDBException {
        String[] processedQuery = TextProcessing.cleanRawWords(query);
        if (processedQuery.length == 0) {
            return new ArrayList<>();
        }
        List<ColumnFamilyHandle> colHandlesList = Collections.nCopies(processedQuery.length, this.rocksDBDao.getWordToWordIdRocksDBCol());

        List<byte[]> wordIdByteList = rocksDBDao.getRocksDB().multiGetAsList(
                colHandlesList,
                Arrays.stream(processedQuery)
                        .map(String::getBytes)
                        .collect(Collectors.toList())
        );

        // filter words that are not indexed, as we are not going to find anything from the database
        wordIdByteList = wordIdByteList
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<String> wordIdList = wordIdByteList.stream()
                .map(String::new)
                .sorted()
                .collect(Collectors.toList());

        ArrayList<AbstractMap.SimpleEntry<String, Double>> queryVector = Util.transformQueryIntoVector(wordIdList);

        // search inverted file for text body
        InvertedFile invertedFileForBody = new InvertedFile(rocksDBDao, rocksDBDao.getInvertedFileForBodyWordIdToPostingListRocksDBCol());
        HashSet<byte[]> urlIdSetWithAtLeastOneKeywordsInDoc = invertedFileForBody.loadInvertedFileWithWordId(wordIdByteList);

        HashMap<byte[], ArrayList<AbstractMap.SimpleEntry<String, Double>>> urlIdVector = new HashMap<>();
        for (byte[] urlIdByte : urlIdSetWithAtLeastOneKeywordsInDoc) {
            HashMap<String, Double> tfIdfVector = rocksDBDao.getTfIdfScoreData(urlIdByte);
            urlIdVector.put(urlIdByte, Util.transformTfIdfVector(tfIdfVector));
        }

        // build a max heap to get the top 50 results
        PriorityQueue<AbstractMap.SimpleEntry<byte[], Double>> maxHeap = new PriorityQueue<>((p1, p2) -> {
            // compare in descending order
            return p1.getValue().compareTo(p2.getValue()) * -1;
        });

        for (Map.Entry<byte[], ArrayList<AbstractMap.SimpleEntry<String, Double>>> entry : urlIdVector.entrySet()) {
            maxHeap.add(new AbstractMap.SimpleEntry<byte[], Double>(entry.getKey(), Util.computeCosSimScore(queryVector, entry.getValue())));
        }

        int resultLength = Math.min(Constants.getMaxReturnSearchResult(), maxHeap.size());

        List<SearchResultsView> resultsViewArrayList = new ArrayList<SearchResultsView>(resultLength);

        for (int index = 0; index < resultLength; index++) {
            AbstractMap.SimpleEntry<byte[], Double> record = maxHeap.poll();
            if (record == null) {
                System.err.println("search result return null from max heap");
                continue;
            }
            resultsViewArrayList.add(rocksDBDao.getSiteSearchViewWithUrlIdFromByte(record.getKey()).setScore(record.getValue()).toSearchResultView());
        }

        return resultsViewArrayList;
    }

    public List<SearchResultsView> getAllSiteFromDB() throws RocksDBException {
        // search from meta data and extract all scraped site

        RocksDB rocksDB = rocksDBDao.getRocksDB();
        RocksIterator it = rocksDB.newIterator(rocksDBDao.getUrlIdToMetaDataRocksDBCol());
        List<SearchResultsView> returnList = new ArrayList<>();

        for (it.seekToFirst(); it.isValid(); it.next()) {
            String urlId = new String(it.key());
            returnList.add(rocksDBDao.getSiteSearchViewWithUrlId(urlId).updateParentLinks(rocksDBDao, urlId).toSearchResultView());
        }

        return returnList;
    }
}
