package com.comp4321Project.searchEngine.View;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class SiteMetaData implements Serializable {
    private final String pageTitle;
    private final String url;
    private final String lastModifiedDate;
    private final String sizeOfPage;
    private final String keywordFrequencyModelList;
    private final String[] childLinks;
    private Double score;

    private String[] parentLinks;

    public SiteMetaData(String pageTitle, String url, String lastModifiedDate, String sizeOfPage, Double score,
                        String keywordFrequencyModelList, Set<String> childLinks) {
        this.pageTitle = pageTitle;
        this.url = url;
        this.lastModifiedDate = lastModifiedDate;
        this.sizeOfPage = sizeOfPage;
        this.score = score;
        this.keywordFrequencyModelList = keywordFrequencyModelList;
        this.childLinks = childLinks.toArray(new String[0]);
        this.parentLinks = null;
    }

    @Override
    public String toString() {
        return "SiteMetaData{" +
                "pageTitle='" + pageTitle + '\'' +
                ", url='" + url + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", sizeOfPage='" + sizeOfPage + '\'' +
                ", keywordFrequencyModelList='" + keywordFrequencyModelList + '\'' +
                ", childLinks=" + Arrays.toString(childLinks) +
                ", score=" + score +
                ", parentLinks=" + Arrays.toString(parentLinks) +
                '}';
    }

    public SiteMetaData updateParentLinks(RocksDBDao rocksDBDao, String urlId) throws RocksDBException {
        RocksDB rocksDB = rocksDBDao.getRocksDB();

        // get parentLinksString
        try {
            HashSet<String> parentLinksIdSet = rocksDBDao.getChildUrlIdToParentUrlIdList(urlId);
            // map url id list to url list
            List<ColumnFamilyHandle> colHandleList = Collections.nCopies(parentLinksIdSet.size(), rocksDBDao.getUrlIdToUrlRocksDBCol());

            List<byte[]> parentUrlListByte = rocksDB.multiGetAsList(colHandleList, parentLinksIdSet
                    .stream()
                    .map(String::getBytes)
                    .collect(Collectors.toList())
            );
            this.parentLinks = parentUrlListByte
                    .stream()
                    .map(String::new)
                    .toArray(String[]::new);
        } catch (NullPointerException e) {
            System.err.println("No child index " + urlId + " in ChildUrlIdToParentUrlIdRocksDBCol");
            this.parentLinks = new String[]{"None"};
        }
        return this;
    }

    public SiteMetaData setScore(Double score) {
        this.score = score;
        return this;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getUrl() {
        return url;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getSizeOfPage() {
        return sizeOfPage;
    }

    public Double getScorel() {
        return score;
    }

    public String getKeywordFrequencyModelList() {
        return keywordFrequencyModelList;
    }

    public SearchResultsView toSearchResultView() {
        return new SearchResultsView(
                pageTitle,
                url,
                lastModifiedDate,
                sizeOfPage,
                score,
                keywordFrequencyModelList,
                childLinks,
                parentLinks
        );
    }
}
