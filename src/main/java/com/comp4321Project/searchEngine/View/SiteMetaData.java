package com.comp4321Project.searchEngine.View;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Service.Spider;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SiteMetaData {
    private static final String separator = " |,.| ";
    private final String pageTitle;
    private final String url;
    private final String lastModifiedDate;
    private final String sizeOfPage;
    private final Double score;
    private final String keywordFrequencyModelList;
    private final String childLinks;
    private String parentLinks;

    public SiteMetaData(String pageTitle, String url, String lastModifiedDate, String sizeOfPage, Double score,
                        String keywordFrequencyModelList, String childLinks) {
        this.pageTitle = pageTitle;
        this.url = url;
        this.lastModifiedDate = lastModifiedDate;
        this.sizeOfPage = sizeOfPage;
        this.score = score;
        this.keywordFrequencyModelList = keywordFrequencyModelList;
        this.childLinks = childLinks;
        this.parentLinks = null;
    }

    public static SiteMetaData fromMetaDataString(String metaDataString) {
        List<String> splitResult = Splitter.on(separator).splitToList(metaDataString);

        return new SiteMetaData(splitResult.get(0), splitResult.get(1), splitResult.get(2), splitResult.get(3), -1.0, splitResult.get(4), splitResult.get(5));
    }

    public String getParentLinksString() {
        if (parentLinks == null) {
            return "Links not fetched from database";
        } else {
            return parentLinks;
        }
    }

    public String[] getParentLinks() {
        if (parentLinks == null) {
            return null;
        } else {
            return parentLinks.split("\n");
        }
    }

    public String toMetaDataString() {
        return String.format("%s |,.| %s |,.| %s |,.| %s |,.| %s |,.| %s", pageTitle, url, lastModifiedDate, sizeOfPage, keywordFrequencyModelList, childLinks);
    }

    public SiteMetaData updateParentLinks(RocksDBDao rocksDBDao, String urlId) throws RocksDBException {
        RocksDB rocksDB = rocksDBDao.getRocksDB();

        // get parentLinksString
        try {
            String parentLinksIdString = new String(rocksDB.get(rocksDBDao.getChildUrlIdToParentUrlIdRocksDBCol(), urlId.getBytes()));
            List<String> parentLinksIdList = Splitter.on(Spider.getSpaceSeparator()).splitToList(parentLinksIdString);
            // map url id list to url list
            List<ColumnFamilyHandle> colHandleList = new ArrayList<>();
            for (int index = 0; index < parentLinksIdList.size(); index++) {
                colHandleList.add(rocksDBDao.getUrlIdToUrlRocksDBCol());
            }

            List<byte[]> parentUrlListByte = rocksDB.multiGetAsList(colHandleList, parentLinksIdList.stream().map(String::getBytes).collect(Collectors.toList()));
            List<String> parentUrlList = parentUrlListByte.stream().map(String::new).collect(Collectors.toList());
            this.parentLinks = Joiner.on('\n').join(parentUrlList);
        } catch (NullPointerException e) {
            System.err.println("No child index " + urlId + " in ChildUrlIdToParentUrlIdRocksDBCol");
            this.parentLinks = "None";
        }
        return this;
    }

//    public String toPrint() {
//        String printString = "";
//        printString += this.pageTitle + "\n";
//        printString += this.url + "\n";
//        printString += this.lastModifiedDate + ", " + sizeOfPage + "\n";
//        printString += this.keywordFrequencyModelList + "\n";
//        printString += "Parent Links:\n";
//        printString += this.parentLinks + "\n";
//        printString += "Child Links\n";
//        printString += this.childLinks + "\n";
//
//        return printString;
//    }

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

    public String getChildLinksString() {
        return childLinks;
    }

    public String[] getChildLinks() {
        return childLinks.split("\n");
    }

    public SearchResultsView toSearchResultView() {
        return new SearchResultsView(
                pageTitle,
                url,
                lastModifiedDate,
                sizeOfPage,
                score,
                keywordFrequencyModelList,
                getChildLinks(),
                getParentLinks()
        );
    }
}
