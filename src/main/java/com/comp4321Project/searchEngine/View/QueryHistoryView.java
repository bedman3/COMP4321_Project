package com.comp4321Project.searchEngine.View;

import java.io.Serializable;

public class QueryHistoryView implements Serializable {
    String rawQuery;

    public QueryHistoryView(String rawQuery, QuerySearchResponseView queryResponse) {
        this.rawQuery = rawQuery;
        this.queryResponse = queryResponse;
    }

    QuerySearchResponseView queryResponse;

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
