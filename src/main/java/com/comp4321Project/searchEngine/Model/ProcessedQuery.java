package com.comp4321Project.searchEngine.Model;

import java.util.Arrays;

public class ProcessedQuery {
    String[][] phrases;
    String[] query;

    public String[] getFilteredQuery() {
        return filteredQuery;
    }

    String[] filteredQuery;

    public ProcessedQuery(String[][] phrases, String[] query, String[] filteredQuery) {
        this.phrases = phrases;
        this.query = query;
        this.filteredQuery = filteredQuery;
    }

    public String[][] getPhrases() {
        return phrases;
    }

    public String[] getQuery() {
        return query;
    }

    private String[] joinPhrasesArrayToString(String[][] phrases) {
        return Arrays.stream(phrases).map(onePhrase -> String.join(" ", onePhrase)).toArray(String[]::new);
    }

    public boolean isPhraseEqual(String[][] thatPhrase) {
        if (thatPhrase.length != this.phrases.length) return false;
        String[] thisArr = joinPhrasesArrayToString(this.phrases), thatArr = joinPhrasesArrayToString(thatPhrase);
        return Arrays.equals(thisArr, thatArr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessedQuery that = (ProcessedQuery) o;
        return Arrays.equals(query, that.query) && isPhraseEqual(that.phrases);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(phrases);
        result = 31 * result + Arrays.hashCode(query);
        return result;
    }
}
