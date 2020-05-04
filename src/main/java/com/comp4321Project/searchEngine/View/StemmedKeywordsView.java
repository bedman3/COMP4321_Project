package com.comp4321Project.searchEngine.View;

import java.util.ArrayList;

public class StemmedKeywordsView {
    private final ArrayList<String> stemmedKeywordList;

    public StemmedKeywordsView(ArrayList<String> stemmedKeywordCache) {
        this.stemmedKeywordList = stemmedKeywordCache;
    }

    @Override
    public String toString() {
        return "StemmedKeywordsView{" +
                "stemmedKeywordList=" + stemmedKeywordList +
                '}';
    }

    public ArrayList<String> getStemmedKeywordList() {
        return stemmedKeywordList;
    }
}
