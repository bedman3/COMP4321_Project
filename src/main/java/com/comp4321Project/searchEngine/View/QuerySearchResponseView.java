package com.comp4321Project.searchEngine.View;

import java.io.Serializable;
import java.util.List;

public class QuerySearchResponseView implements Serializable {
    int totalNumOfResult;
    double totalTimeUsed;
    List<SearchResultsView> searchResults;

    public void setTotalTimeUsed(double totalTimeUsed) {
        this.totalTimeUsed = totalTimeUsed;
    }

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
