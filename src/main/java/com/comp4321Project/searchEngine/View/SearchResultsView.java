package com.comp4321Project.searchEngine.View;

import java.io.Serializable;
import java.util.Arrays;

public class SearchResultsView implements Serializable {
    public String pageTitle;
    public String url;
    public String lastModifiedDate;
    public String sizeOfPage;
    public Double score;
    public String[][] keywordFrequencyModelList;
    public String[] childLinks;
    public String[] parentLinks;

    public SearchResultsView(String pageTitle,
                             String url,
                             String lastModifiedDate,
                             String sizeOfPage,
                             Double score,
                             String keywordFrequencyModelList,
                             String[] childLinks,
                             String[] parentLinks) {
        this.pageTitle = pageTitle;
        this.url = url;
        this.lastModifiedDate = lastModifiedDate;
        this.sizeOfPage = sizeOfPage;
        this.score = score;
        this.childLinks = childLinks;
        this.parentLinks = parentLinks;

        this.keywordFrequencyModelList = Arrays.stream(keywordFrequencyModelList
                .split(";"))
                .filter(str -> !str.equals(""))
                .map(pairStr -> {
                    String[] pair = pairStr.split(" ");
                    return new String[]{pair[0], pair[1]};
                })
                .toArray(size -> new String[size][2]);
    }

    @Override
    public String toString() {
        return "SearchResultsView{" +
                "pageTitle='" + pageTitle + '\'' +
                ", url='" + url + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", sizeOfPage='" + sizeOfPage + '\'' +
                ", score=" + score +
                ", keywordFrequencyModelList='" + Arrays.deepToString(keywordFrequencyModelList) + '\'' +
                ", childLinks=" + Arrays.toString(childLinks) +
                ", parentLinks=" + Arrays.toString(parentLinks) +
                '}';
    }
}
