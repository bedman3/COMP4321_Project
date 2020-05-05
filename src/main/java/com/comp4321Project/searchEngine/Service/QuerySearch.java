package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Model.ProcessedQuery;
import com.comp4321Project.searchEngine.Util.TextProcessing;
import com.comp4321Project.searchEngine.Util.Util;
import com.comp4321Project.searchEngine.View.QueryHistoryView;
import com.comp4321Project.searchEngine.View.QuerySearchResponseView;
import com.comp4321Project.searchEngine.View.SearchResultsView;
import com.comp4321Project.searchEngine.View.StemmedKeywordsView;
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

        InvertedFile invertedFileForBody = new InvertedFile(rocksDBDao, rocksDBDao.getInvertedFileForBodyWordIdToPostingListRocksDBCol());
        InvertedFile invertedFileForTitle = new InvertedFile(rocksDBDao, rocksDBDao.getInvertedFileForTitleWordIdToPostingListRocksDBCol());
        HashSet<String> urlIdSetWithAtLeastOneKeywordInBody;
        HashSet<String> urlIdSetWithAtLeastOneKeywordInTitle;

        assert processedQuery != null;
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

        if (phrasesQuery != null) {
            // if there is phrase in a query

            // extract word id for words in phrases
            List<String> getWordIdList = new ArrayList<>();
            Arrays.stream(phrasesQuery).forEach(strArr -> getWordIdList.addAll(Arrays.asList(strArr)));

            colHandlesList = Collections.nCopies(getWordIdList.size(), this.rocksDBDao.getWordToWordIdRocksDBCol());

            List<byte[]> wordIdForPhraseByteList = rocksDBDao.getRocksDB().multiGetAsList(
                    colHandlesList,
                    getWordIdList
                            .stream()
                            .map(String::getBytes)
                            .collect(Collectors.toCollection(ArrayList::new))
            );
            HashMap<String, byte[]> hashMap = new HashMap<>();
            for (int index = 0; index < getWordIdList.size(); index++) {
                hashMap.put(getWordIdList.get(index), wordIdForPhraseByteList.get(index));
            }

            // check which phrase contains word without wordId, then filter out that phrase
            ArrayList<ArrayList<String>> phrasesListInWordId = new ArrayList<>();
            for (String[] phrase : phrasesQuery) {
                phrasesListInWordId.add(Arrays.stream(phrase)
                        .map(hashMap::get)
                        .filter(Objects::nonNull)
                        .map(String::new)
                        .collect(Collectors.toCollection(ArrayList::new))
                );
            }

            // process phrases according to phrasesListInWordId
            urlIdSetWithAtLeastOneKeywordInBody = invertedFileForBody.loadInvertedFileWithPhrases(phrasesListInWordId);
            urlIdSetWithAtLeastOneKeywordInTitle = invertedFileForTitle.loadInvertedFileWithPhrases(phrasesListInWordId);
        } else {
            // search inverted file for text body and for title
            urlIdSetWithAtLeastOneKeywordInBody = invertedFileForBody.loadInvertedFileWithWordId(wordIdByteList);
            urlIdSetWithAtLeastOneKeywordInTitle = invertedFileForTitle.loadInvertedFileWithWordId(wordIdByteList);
        }

        // calculate total num of results from body
        int totalNumOfResult = urlIdSetWithAtLeastOneKeywordInBody.size() + urlIdSetWithAtLeastOneKeywordInTitle.size();

        // calculate the composite score before ranking
        HashMap<String, Double> compositeScore = new HashMap<>();

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
            Double pageRankScore = rocksDBDao.getPageRankScore(entry.getKey()) * Constants.getPageRankMultiplier();
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

    public StemmedKeywordsView getAllStemmedKeywords() throws RocksDBException {
        return new StemmedKeywordsView(rocksDBDao.getStemmedKeywordCache());
    }

    public List<QueryHistoryView> getQueryHistory(int limit) throws RocksDBException {
        byte[] nextAvailableIdByte = rocksDBDao.getRocksDB().get(rocksDBDao.getQueryHistoryRocksDBCol(), Constants.getNextAvailableIdLiteral().getBytes());
        Integer totalNumOfHistory = Integer.parseInt(new String(nextAvailableIdByte));

        Integer start;
        if (totalNumOfHistory - limit >= 0) start = totalNumOfHistory - limit;
        else start = 0;

        ArrayList<QueryHistoryView> queryHistoryViews = new ArrayList<>();
        for (Integer index = start; index < totalNumOfHistory; index++) {
            queryHistoryViews.add(rocksDBDao.getQueryHistory(index.toString()));
        }

        return queryHistoryViews;
    }
}
