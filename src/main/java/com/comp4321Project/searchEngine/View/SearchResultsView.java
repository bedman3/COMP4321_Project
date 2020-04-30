package com.comp4321Project.searchEngine.View;

public class SearchResultsView {
    public String pageTitle;
    public String url;
    public String lastModifiedDate;
    public String sizeOfPage;
    public Double score;
    public String keywordFrequencyModelList;
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
        this.keywordFrequencyModelList = keywordFrequencyModelList;
        this.childLinks = childLinks;
        this.parentLinks = parentLinks;
    }
}
