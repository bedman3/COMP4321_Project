package com.comp4321Project.searchEngine.Model;

public class KeywordFrequencyModel {
    private final String keyword;
    private Integer frequency;

    public KeywordFrequencyModel(String keyword, Integer frequency) {
        this.keyword = keyword;
        this.frequency = frequency;
    }

    public String getKeyword() {
        return keyword;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void freqIncrement(Integer freq) {
        this.frequency += freq;
    }

    public void freqIncrement() {
        this.frequency++;
    }
}
