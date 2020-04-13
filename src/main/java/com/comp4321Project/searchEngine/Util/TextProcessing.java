package com.comp4321Project.searchEngine.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TextProcessing {
    private final String[] stopWords;
    private Set<String> stopWordsSet;

    public TextProcessing() {
        this.stopWords = new String[]{
                "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
                "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers",
                "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves",
                "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are",
                "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does",
                "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until",
                "while", "of", "at", "by", "for", "with", "about", "against", "between", "into",
                "through", "during", "before", "after", "above", "below", "to", "from", "up", "down",
                "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here",
                "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more",
                "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so",
                "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"
        };
        this.stopWordsSet = new HashSet<String>(Arrays.asList(stopWords));
    }

    public Set<String> getStopWordsSet() {
        return stopWordsSet;
    }

    public boolean isStopWord(String word) {
        return this.stopWordsSet.contains(word);
    }
}
