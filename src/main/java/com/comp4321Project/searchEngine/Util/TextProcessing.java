package com.comp4321Project.searchEngine.Util;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class TextProcessing {
    private static final String[] stopWords;
    private static final Set<String> stopWordsSet;
    private static final Porter porterObject;

    static {
        porterObject = new Porter();
    }

    static {
        stopWords = new String[]{"a", "about", "above", "across", "after", "again", "against", "all", "almost",
                "alone", "along", "already", "also", "although", "always", "among", "an", "and", "another", "any",
                "anybody", "anyone", "anything", "anywhere", "are", "area", "areas", "around", "as", "ask", "asked",
                "asking", "asks", "at", "away", "b", "back", "backed", "backing", "backs", "be", "became", "because",
                "become", "becomes", "been", "before", "began", "behind", "being", "beings", "best", "better",
                "between", "big", "both", "but", "by", "c", "came", "can", "cannot", "case", "cases", "certain",
                "certainly", "clear", "clearly", "come", "could", "d", "did", "differ", "different", "differently",
                "do", "does", "done", "down", "downed", "downing", "downs", "during", "e", "each", "early", "either",
                "end", "ended", "ending", "ends", "enough", "even", "evenly", "ever", "every", "everybody", "everyone",
                "everything", "everywhere", "f", "face", "faces", "fact", "facts", "far", "felt", "few", "find",
                "finds", "first", "for", "four", "from", "full", "fully", "further", "furthered", "furthering",
                "furthers", "g", "gave", "general", "generally", "get", "gets", "give", "given", "gives", "go",
                "going", "good", "goods", "got", "great", "greater", "greatest", "group", "grouped", "grouping",
                "groups", "h", "had", "has", "have", "having", "he", "her", "here", "herself", "high", "higher",
                "highest", "him", "himself", "his", "how", "however", "i", "if", "important", "in", "interest",
                "interested", "interesting", "interests", "into", "is", "it", "its", "itself", "j", "just", "k",
                "keep", "keeps", "kind", "knew", "know", "known", "knows", "l", "large", "largely", "last", "later",
                "latest", "least", "less", "let", "lets", "like", "likely", "long", "longer", "longest", "m", "made",
                "make", "making", "man", "many", "may", "me", "member", "members", "men", "might", "more", "most",
                "mostly", "mr", "mrs", "much", "must", "my", "myself", "n", "necessary", "need", "needed", "needing",
                "needs", "never", "new", "newer", "newest", "next", "no", "nobody", "non", "noone", "not", "nothing",
                "now", "nowhere", "number", "numbers", "o", "of", "off", "often", "old", "older", "oldest", "on",
                "once", "one", "only", "open", "opened", "opening", "opens", "or", "order", "ordered", "ordering",
                "orders", "other", "others", "our", "out", "over", "p", "part", "parted", "parting", "parts", "per",
                "perhaps", "place", "places", "point", "pointed", "pointing", "points", "possible", "present",
                "presented", "presenting", "presents", "problem", "problems", "put", "puts", "q", "quite", "r",
                "rather", "really", "right", "room", "rooms", "s", "said", "same", "saw", "say", "says", "second",
                "seconds", "see", "seem", "seemed", "seeming", "seems", "sees", "several", "shall", "she", "should",
                "show", "showed", "showing", "shows", "side", "sides", "since", "small", "smaller", "smallest", "so",
                "some", "somebody", "someone", "something", "somewhere", "state", "states", "still", "such", "sure",
                "t", "take", "taken", "than", "that", "the", "their", "them", "then", "there", "therefore", "these",
                "they", "thing", "things", "think", "thinks", "this", "those", "though", "thought", "thoughts",
                "three", "through", "thus", "to", "today", "together", "too", "took", "toward", "turn", "turned",
                "turning", "turns", "two", "u", "under", "until", "up", "upon", "us", "use", "used", "uses", "v",
                "very", "w", "want", "wanted", "wanting", "wants", "was", "way", "ways", "we", "well", "wells", "went",
                "were", "what", "when", "where", "whether", "which", "while", "who", "whole", "whose", "why", "will",
                "with", "within", "without", "work", "worked", "working", "works", "would", "x", "y", "year", "years",
                "yet", "you", "young", "younger", "youngest", "your", "yours", "z", ""};
        stopWordsSet = new HashSet<String>(Arrays.asList(stopWords));
    }


    public static Set<String> getStopWordsSet() {
        return stopWordsSet;
    }

    public static boolean isStopWord(String word) {
        return stopWordsSet.contains(word);
    }

    public static boolean isNotStopWord(String word) {
        return !isStopWord(word);
    }

    /**
     * This function will:
     * 1) convert all words to lowercase
     * 2) remove punctuations
     * 3) tokenize words into array
     * 4) remove stop words
     * 5) stem words using porter's algorithm
     *
     * @return cleaned string array
     */
    public static String[] cleanRawWords(String rawText) {
        String[] tokenizedWordArray = rawText
                .toLowerCase()
                .replaceAll("\\p{P}", "")
                .replaceAll("\\s+", " ") // remove extra spaces left due to removal of punctuations
                .split(" ");

        // remove stop words
        tokenizedWordArray = Arrays.stream(tokenizedWordArray)
                .filter(TextProcessing::isNotStopWord)
                .map(porterObject::stripAffixes) // porter's algorithm
                .toArray(String[]::new);
        return tokenizedWordArray.length == 0 ? null : tokenizedWordArray;
    }

    /**
     * clean the query by calling cleanRawWords() and separate phrases specified by double quotes
     * @param rawQuery
     * @return
     */
    public static Pair<String[][], String[]> cleanRawQuery(String rawQuery) {
        if (!rawQuery.contains("\"")) return new ImmutablePair<>(null, cleanRawWords(rawQuery));

        List<String> phrasesList = new LinkedList<>();
        StringBuilder stringBuilder = new StringBuilder();
        int startPtr = -1, endPtr = 0, markPtr = 0, rawQueryLength = rawQuery.length();
        while (endPtr < rawQueryLength) {
            if (rawQuery.charAt(endPtr) == '\"') {
                if (startPtr == -1) {
                    // mark the beginning of the quote
                    startPtr = endPtr;
                } else {
                    // there is a quote pair, extract the sentence in the quote
                    phrasesList.add(rawQuery.substring(startPtr, endPtr));
                    if (startPtr != markPtr) {
                        stringBuilder.append(rawQuery, markPtr, startPtr);
                    }
                    markPtr = endPtr + 1;
                    startPtr = -1;
                }
            }
            endPtr++;
        }

        if (markPtr < endPtr) {
            stringBuilder.append(rawQuery, markPtr, endPtr);
        }

        String[][] returnPhrase = phrasesList.stream()
                .map(TextProcessing::cleanRawWords)
                .filter(Objects::nonNull)
                .toArray(String[][]::new);

        return new ImmutablePair<>(returnPhrase.length == 0 ? null : returnPhrase, cleanRawWords(stringBuilder.toString()));
    }
}
