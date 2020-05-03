package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Util.TextProcessing;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.QuerySearchResponseView;
import com.comp4321Project.searchEngine.View.SearchResultsView;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class QuerySearch {
    RocksDBDao rocksDBDao;

    public QuerySearch(RocksDBDao rocksDBDao) {
        this.rocksDBDao = rocksDBDao;
    }

    public QuerySearchResponseView search(String query) throws RocksDBException {
        String[][] phrasesQuery = TextProcessing.getPhrasesFromQuery(query);
        String[] processedQuery = TextProcessing.cleanRawWords(query);
        if (processedQuery.length == 0) {
            // skip processing when there is no query words left
            return new QuerySearchResponseView(-1, null);
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

        ArrayList<Pair<String, Double>> queryVector = Util.transformQueryIntoVector(wordIdList);

        // search inverted file for text body
        InvertedFile invertedFileForBody = new InvertedFile(rocksDBDao, rocksDBDao.getInvertedFileForBodyWordIdToPostingListRocksDBCol());
        HashSet<String> urlIdSetWithAtLeastOneKeywordsInDoc = invertedFileForBody.loadInvertedFileWithWordId(wordIdByteList);

        int totalNumOfResult = urlIdSetWithAtLeastOneKeywordsInDoc.size();

        HashMap<byte[], ArrayList<Pair<String, Double>>> urlIdVector = new HashMap<>();
        for (String urlId : urlIdSetWithAtLeastOneKeywordsInDoc) {
            HashMap<String, Double> tfIdfVector = rocksDBDao.getTfIdfScoreData(urlId.getBytes());
            urlIdVector.put(urlId.getBytes(), Util.transformTfIdfVector(tfIdfVector));
        }

        // build a max heap to get the top 50 results
        PriorityQueue<Pair<byte[], Double>> maxHeap = new PriorityQueue<>((p1, p2) -> {
            // compare in descending order
            return p1.getValue().compareTo(p2.getValue()) * -1;
        });

        for (Map.Entry<byte[], ArrayList<Pair<String, Double>>> entry : urlIdVector.entrySet()) {
            maxHeap.add(new ImmutablePair<>(entry.getKey(), Util.computeCosSimScore(queryVector, entry.getValue())));
        }

        int resultLength = Math.min(Constants.getMaxReturnSearchResult(), maxHeap.size());

        List<SearchResultsView> resultsViewArrayList = new ArrayList<SearchResultsView>(resultLength);

        for (int index = 0; index < resultLength; index++) {
            Pair<byte[], Double> record = maxHeap.poll();
            if (record == null) {
                System.err.println("search result return null from max heap");
                continue;
            }
            resultsViewArrayList.add(rocksDBDao.getSiteSearchViewWithUrlIdFromByte(record.getKey())
                    .setScore(record.getValue())
                    .updateParentLinks(rocksDBDao, new String(record.getKey()))
                    .toSearchResultView());
        }

        return new QuerySearchResponseView(totalNumOfResult, resultsViewArrayList);
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
