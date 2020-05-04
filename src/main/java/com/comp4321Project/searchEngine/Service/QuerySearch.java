package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Model.ProcessedQuery;
import com.comp4321Project.searchEngine.Util.TextProcessing;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.QuerySearchResponseView;
import com.comp4321Project.searchEngine.View.SearchResultsView;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
        long startMillis = System.currentTimeMillis();

        ProcessedQuery processedQueryPair = TextProcessing.cleanRawQuery(query);

        // check if there is any cache available, if so return the cache
        QuerySearchResponseView querySearchResponseView = rocksDBDao.getQueryCache(processedQueryPair);
        if (querySearchResponseView != null) {
            // found cache and return
            querySearchResponseView.setTotalTimeUsed(Util.getTotalTimeUsedInSecond(startMillis));
            return querySearchResponseView;
        }

        // cache does not exists, query the results from rocksdb
        String[][] phrasesQuery = processedQueryPair.getPhrases();
        String[] processedQuery = processedQueryPair.getQuery();
        if (processedQuery == null && phrasesQuery == null) {
            // skip processing when there is no query words left
            return new QuerySearchResponseView(-1, -1, null);
        }

        if (phrasesQuery != null) {
            // if there is phrase in a query

            return null;
        } else {
            List<ColumnFamilyHandle> colHandlesList = Collections.nCopies(processedQuery.length, this.rocksDBDao.getWordToWordIdRocksDBCol());

            List<byte[]> wordIdByteList = rocksDBDao.getRocksDB().multiGetAsList(
                    colHandlesList,
                    Arrays.stream(processedQuery)
                            .map(String::getBytes)
                            .collect(Collectors.toList())
            );

            // filter words that are not indexed, as we are not going to find anything from the database
            // and map the byte to string
            List<String> wordIdList = wordIdByteList
                    .stream()
                    .filter(Objects::nonNull)
                    .map(String::new)
                    .sorted()
                    .collect(Collectors.toList());

            if (wordIdList.size() == 0) return new QuerySearchResponseView(-1, -1, null);

            ArrayList<ImmutablePair<String, Double>> queryVector = Util.transformQueryToVector(wordIdList);

            // search inverted file for text body and for title
            InvertedFile invertedFileForBody = new InvertedFile(rocksDBDao, rocksDBDao.getInvertedFileForBodyWordIdToPostingListRocksDBCol());
            InvertedFile invertedFileForTitle = new InvertedFile(rocksDBDao, rocksDBDao.getInvertedFileForTitleWordIdToPostingListRocksDBCol());
            HashSet<String> urlIdSetWithAtLeastOneKeywordInBody = invertedFileForBody.loadInvertedFileWithWordId(wordIdByteList);
            HashSet<String> urlIdSetWithAtLeastOneKeywordInTitle = invertedFileForTitle.loadInvertedFileWithWordId(wordIdByteList);

            // make a union set for title and body
//            HashSet<String> urlIdSetWithAtLeastOneKeywordInTheWholeDocument = new HashSet<>(urlIdSetWithAtLeastOneKeywordInBody);
//            urlIdSetWithAtLeastOneKeywordInTheWholeDocument.addAll(urlIdSetWithAtLeastOneKeywordInTitle);

            // calculate total num of results from body
            int totalNumOfResult = urlIdSetWithAtLeastOneKeywordInBody.size() + urlIdSetWithAtLeastOneKeywordInTitle.size();

            // calculate the composite score before ranking
            HashMap<String, Double> compositeScore = new HashMap<>();

//            HashMap<String, ArrayList<ImmutablePair<String, Double>>> urlIdVector = new HashMap<>();
            for (String urlId : urlIdSetWithAtLeastOneKeywordInBody) {
                ArrayList<ImmutablePair<String, Double>> vector = rocksDBDao.getTfIdfScoreData(urlId.getBytes());
                Double score = Util.computeCosSimScore(queryVector, vector);
                compositeScore.put(urlId, score);
            }

            // account for the score got from title
            for (String urlId : urlIdSetWithAtLeastOneKeywordInTitle) {
                ArrayList<ImmutablePair<String, Double>> vector = rocksDBDao.getKeywordFrequencyVectorForTitle(urlId);
                Double score = Util.computeCosSimScore(queryVector, vector) * Constants.getTitleMultiplier();
                compositeScore.merge(urlId, score, Double::sum);
            }

            // account for the page rank score
            for (Map.Entry<String, Double> entry : compositeScore.entrySet()) {
                Double pageRankScore = rocksDBDao.getPageRankScore(entry.getKey());
                entry.setValue(entry.getValue() + pageRankScore);
            }

            // build a max heap to get the top 50 results
            PriorityQueue<ImmutablePair<String, Double>> maxHeap = new PriorityQueue<>((p1, p2) -> {
                // compare in descending order
                return p1.getValue().compareTo(p2.getValue()) * -1;
            });

            // feed score map to max heap to find the top 50 scores
            compositeScore.forEach((urlId, totalScore) -> maxHeap.add(new ImmutablePair<>(urlId, totalScore)));

            int resultLength = Math.min(Constants.getMaxReturnSearchResult(), maxHeap.size());

            List<SearchResultsView> resultsViewArrayList = new ArrayList<SearchResultsView>(resultLength);

            for (int index = 0; index < resultLength; index++) {
                ImmutablePair<String, Double> record = maxHeap.poll();
                if (record == null) {
                    System.err.println("search result return null from max heap");
                    continue;
                }
                resultsViewArrayList.add(rocksDBDao.getSiteMetaData(record.getKey())
                        .setScore(record.getValue())
                        .updateParentLinks(rocksDBDao, record.getKey())
                        .toSearchResultView());
            }

            // save the cache in the database
            querySearchResponseView = new QuerySearchResponseView(
                    totalNumOfResult,
                    Util.getTotalTimeUsedInSecond(startMillis),
                    resultsViewArrayList
                    );
            rocksDBDao.putQueryCache(processedQueryPair, querySearchResponseView);

            return querySearchResponseView;
        }
    }

    public List<SearchResultsView> getAllSiteFromDB() throws RocksDBException {
        // search from meta data and extract all scraped site

        RocksDB rocksDB = rocksDBDao.getRocksDB();
        RocksIterator it = rocksDB.newIterator(rocksDBDao.getUrlIdToMetaDataRocksDBCol());
        List<SearchResultsView> returnList = new ArrayList<>();

        for (it.seekToFirst(); it.isValid(); it.next()) {
            String urlId = new String(it.key());
            returnList.add(rocksDBDao.getSiteMetaData(urlId).updateParentLinks(rocksDBDao, urlId).toSearchResultView());
        }

        return returnList;
    }
}
