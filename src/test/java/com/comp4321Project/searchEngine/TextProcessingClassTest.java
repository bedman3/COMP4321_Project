package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Util.TextProcessing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TextProcessingClassTest {
    @Test
    public void testIsStopWord() {
        String[] stopWordsList = TextProcessing.cleanRawWords("I Of On tHe a hE shE nOt ThEn");
        String[] nonStopWordsList = new String[]{"computer", "comput", "visual", "vision", "machine"};


        Arrays.stream(stopWordsList).forEach(str -> Assertions.assertTrue(TextProcessing.isStopWord(str)));
        Arrays.stream(nonStopWordsList).forEach(str -> Assertions.assertTrue(TextProcessing.isNotStopWord(str)));
    }

    @Test
    public void testCleanRawWords() {
        String rawSentence = "I gO To School by bus, ComPuter      Machine, ViSuAlization";
        String expectedSentence = "school bu comput machin visual";

        System.out.println(Arrays.toString(TextProcessing.cleanRawWords(rawSentence)));

        Assertions.assertArrayEquals(expectedSentence.split(" "), TextProcessing.cleanRawWords(rawSentence));
    }

    @Test
    public void testCleanQuery() {
        String rawQuery = "Computer, VISION !!!!!! machiNe     testing LeArNinG";
        String expectedQuery = "comput vision machin test learn";

        Assertions.assertArrayEquals(expectedQuery.split(" "), TextProcessing.cleanRawWords(rawQuery));
    }

    @Test
    public void testPhrasesQuery() {
        String rawQuery = "\"Computer, VISION\" !!!!!!blanket \"machiNe     testing LeArNinG\" wallet";
        String[][] expectedPhrases = new String[][]{new String[]{"comput", "vision"}, new String[]{"machine", "test", "learn"}};
        String expectedQuery = "blanket wallet";

    }
}
