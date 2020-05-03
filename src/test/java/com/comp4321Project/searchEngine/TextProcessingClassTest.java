package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Util.TextProcessing;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TextProcessingClassTest {
    @Test
    public void testIsStopWord() {
        String[] stopWordsList = "I Of On tHe a hE shE nOt ThEn".toLowerCase().split(" ");
        String[] nonStopWordsList = new String[]{"computer", "comput", "visual", "vision", "machine"};


        Arrays.stream(stopWordsList).forEach(str -> Assertions.assertTrue(TextProcessing.isStopWord(str)));
        Arrays.stream(nonStopWordsList).forEach(str -> Assertions.assertTrue(TextProcessing.isNotStopWord(str)));
    }

    @Test
    public void testCleanRawWords() {
        String rawSentence = "I gO To School by bus, ComPuter      Machine, ViSuAlization";
        String expectedSentence = "school bu comput machin visual";

        Assertions.assertArrayEquals(expectedSentence.split(" "), TextProcessing.cleanRawWords(rawSentence));

        Assertions.assertNull(TextProcessing.cleanRawWords(""));
        Assertions.assertNull(TextProcessing.cleanRawWords("      "));
        Assertions.assertNull(TextProcessing.cleanRawWords("  !!!!  "));
        Assertions.assertNull(TextProcessing.cleanRawWords("    !!!!!"));
    }

    @Test
    public void testCleanQuery() {
        String rawQuery = "Computer, VISION !!!!!! machiNe     testing LeArNinG";
        String expectedQuery = "comput vision machin test learn";

        Pair<String[][], String[]> result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(expectedQuery.split(" "), result.getRight());
        Assertions.assertNull(result.getLeft());
    }

    @Test
    public void testPhrasesQuery() {
        String rawQuery = "\"Computer, VISION\" !!!!!!blanket \"machiNe     testing LeArNinG\" wallet";
        String[][] expectedPhrases = new String[][]{new String[]{"comput", "vision"}, new String[]{"machin", "test", "learn"}};
        String expectedQuery = "blanket wallet";

        Pair<String[][], String[]> result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(result.getRight(), expectedQuery.split(" "));
        Assertions.assertTrue(Arrays.deepEquals(result.getLeft(), expectedPhrases));

        rawQuery = "testing \"Computer, VISION\" !!!!!!blanket \"machiNe     testing LeArNinG\" wallet";
        expectedPhrases = new String[][]{new String[]{"comput", "vision"}, new String[]{"machin", "test", "learn"}};
        expectedQuery = "test blanket wallet";

        result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(result.getRight(), expectedQuery.split(" "));
        Assertions.assertTrue(Arrays.deepEquals(result.getLeft(), expectedPhrases));

        rawQuery = "testing \"Computer, VISION\" !!!!!!blanket \"machiNe   !!!!  testing LeArNinG\" \"wallet";
        expectedPhrases = new String[][]{new String[]{"comput", "vision"}, new String[]{"machin", "test", "learn"}};
        expectedQuery = "test blanket wallet";

        result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(result.getRight(), expectedQuery.split(" "));
        Assertions.assertTrue(Arrays.deepEquals(result.getLeft(), expectedPhrases));

        rawQuery = "testing \",\" !!!!!!blanket \"   !!!!  \" \"wallet";
        expectedQuery = "test blanket wallet";

        result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(result.getRight(), expectedQuery.split(" "));
        Assertions.assertNull(result.getLeft());
    }
}
