package com.comp4321Project.searchEngine.View;

import java.util.List;

public class QuerySearchResponseView {
    int totalNumOfResult;
    double totalTimeUsed;
    List<SearchResultsView> searchResults;

    public QuerySearchResponseView(int totalNumOfResult, double totalTimeUsed, List<SearchResultsView> searchResults) {
        this.totalNumOfResult = totalNumOfResult;
        this.totalTimeUsed = totalTimeUsed;
        this.searchResults = searchResults;
    }

    public double getTotalTimeUsed() {
        return totalTimeUsed;
    }

    @Override
    public String toString() {
        return "QuerySearchResponseView{" +
                "totalNumOfResult=" + totalNumOfResult +
                ", totalTimeUsed=" + totalTimeUsed +
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
