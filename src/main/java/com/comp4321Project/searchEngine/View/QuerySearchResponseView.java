package com.comp4321Project.searchEngine.View;

import java.util.List;

public class QuerySearchResponseView {
    int totalNumOfResult;
    List<SearchResultsView> searchResults;

    public QuerySearchResponseView(int totalNumOfResult, List<SearchResultsView> searchResults) {
        this.totalNumOfResult = totalNumOfResult;
        this.searchResults = searchResults;
    }

    @Override
    public String toString() {
        return "QuerySearchResponseView{" +
                "totalResult=" + totalNumOfResult +
                ", searchResults=" + searchResults +
                '}';
    }

    public int getTotalNumOfResult() {
        return totalNumOfResult;
    }

    public List<SearchResultsView> getSearchResults() {
        return searchResults;
    }
}
