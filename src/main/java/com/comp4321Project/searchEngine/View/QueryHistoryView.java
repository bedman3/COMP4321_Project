package com.comp4321Project.searchEngine.View;

import java.io.Serializable;

public class QueryHistoryView implements Serializable {
    String rawQuery;

    public QueryHistoryView(String rawQuery, QuerySearchResponseView querySearchResponseView) {
        this.rawQuery = rawQuery;
        this.querySearchResponseView = querySearchResponseView;
    }

    QuerySearchResponseView querySearchResponseView;

    public String getRawQuery() {
        return rawQuery;
    }

    public QuerySearchResponseView getQuerySearchResponseView() {
        return querySearchResponseView;
    }

    @Override
    public String toString() {
        return "QueryHistoryView{" +
                "rawQuery='" + rawQuery + '\'' +
                ", querySearchResponseView=" + querySearchResponseView +
                '}';
    }
}
