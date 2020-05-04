package com.comp4321Project.searchEngine.View;

import java.io.Serializable;

public class QueryHistoryView implements Serializable {
    String rawQuery;
    QuerySearchResponseView queryResponse;

    public QueryHistoryView(String rawQuery, QuerySearchResponseView queryResponse) {
        this.rawQuery = rawQuery;
        this.queryResponse = queryResponse;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public QuerySearchResponseView getQueryResponse() {
        return queryResponse;
    }

    @Override
    public String toString() {
        return "QueryHistoryView{" +
                "rawQuery='" + rawQuery + '\'' +
                ", querySearchResponseView=" + queryResponse +
                '}';
    }
}
