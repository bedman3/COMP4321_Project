package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;
import com.comp4321Project.searchEngine.Util.TextProcessing;
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
        RocksDB rocksDB = rocksDBDao.getRocksDB();
        String[] processedQuery = TextProcessing.cleanRawWords(query);

        List<ColumnFamilyHandle> colHandlesList = Collections.nCopies(processedQuery.length, this.rocksDBDao.getWordToWordIdRocksDBCol());

        List<byte[]> wordIdList = rocksDBDao.getRocksDB().multiGetAsList(
                colHandlesList,
                Arrays.stream(processedQuery)
                        .map(String::getBytes)
                        .collect(Collectors.toList())
        );

        // filter words that are not indexed, as we are not going to find anything from the database
        wordIdList = wordIdList
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // search inverted file for text body
        InvertedFile invertedFileForBody = new InvertedFile(rocksDBDao, rocksDBDao.getInvertedFileForBodyWordIdToPostingListRocksDBCol());
        HashSet<byte[]> urlIdSetWithAtLeastOneKeywordsInDoc = invertedFileForBody.loadInvertedFileWithWordId(wordIdList);

        HashMap<byte[], HashMap<String, Double>> urlIdVector = new HashMap<>();
        for (byte[] urlIdByte : urlIdSetWithAtLeastOneKeywordsInDoc) {
            urlIdVector.put(urlIdByte, rocksDBDao.getKeywordTermFrequencyData(urlIdByte));
        }


//        String urlId = rocksDBDao.getUrlIdFromUrl(url);
//
//        SiteMetaData siteMetaData = rocksDBDao.getSiteSearchViewWithUrlId(urlId);
//        siteMetaData.updateParentLinks(rocksDBDao, urlId);
//
//        return siteMetaData;
        return null;
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
