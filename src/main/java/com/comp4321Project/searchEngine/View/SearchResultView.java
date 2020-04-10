package com.comp4321Project.searchEngine.View;

import com.comp4321Project.searchEngine.Model.KeywordFrequencyModel;

import java.util.List;

public class SearchResultView {
    private final String pageTitle;
    private final String url;
    private final String lastModifiedDate;
    private final Integer sizeOfPage;
    private final Float score;
    private final List<KeywordFrequencyModel> keywordFrequencyModelList;
    private final List<String> parentLinks;
    private final List<String> childLinks;

    public String getPageTitle() {
        return pageTitle;
    }

    public String getUrl() {
        return url;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Integer getSizeOfPage() {
        return sizeOfPage;
    }

    public Float getScorel() {
        return score;
    }

    public List<KeywordFrequencyModel> getKeywordFrequencyModelList() {
        return keywordFrequencyModelList;
    }

    public List<String> getParentLinks() {
        return parentLinks;
    }

    public List<String> getChildLinks() {
        return childLinks;
    }

    public SearchResultView(String pageTitle, String url, String lastModifiedDate, Integer sizeOfPage, Float score,
                            List<KeywordFrequencyModel> keywordFrequencyModelList, List<String> parentLinks,
                            List<String> childLinks) {
        this.pageTitle = pageTitle;
        this.url = url;
        this.lastModifiedDate = lastModifiedDate;
        this.sizeOfPage = sizeOfPage;
        this.score = score;
        this.keywordFrequencyModelList = keywordFrequencyModelList;
        this.parentLinks = parentLinks;
        this.childLinks = childLinks;
    }
}
