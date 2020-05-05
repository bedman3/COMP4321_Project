package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Model.ProcessedQuery;
import com.comp4321Project.searchEngine.Util.TextProcessing;
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
        String rawQuery = "Computer,  VISION !!!!!! machiNe     testing LeArNinG";
        String expectedQuery = "comput vision machin test learn";

        ProcessedQuery result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(expectedQuery.split(" "), result.getQuery());
        Assertions.assertNull(result.getPhrases());
        Assertions.assertArrayEquals(expectedQuery.split(" "), result.getFilteredQuery());

        rawQuery = "Computer,  \"VISION !!!!!! machiNe     testing LeArNinG";
        expectedQuery = "comput vision machin test learn";

        result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(expectedQuery.split(" "), result.getQuery());
        Assertions.assertNull(result.getPhrases());
        Assertions.assertArrayEquals(expectedQuery.split(" "), result.getFilteredQuery());
    }

    @Test
    public void testPhrasesQuery() {
        String rawQuery = "\"Computer, VISION\" !!!!!!blanket \"machiNe     testing LeArNinG\" wallet";
        String[][] expectedPhrases = new String[][]{new String[]{"comput", "vision"}, new String[]{"machin", "test", "learn"}};
        String expectedQuery = "comput vision blanket machin test learn wallet";

        ProcessedQuery result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(result.getQuery(), expectedQuery.split(" "));
        Assertions.assertTrue(Arrays.deepEquals(result.getPhrases(), expectedPhrases));

        rawQuery = "testing \"Computer, VISION\" !!!!!!blanket \"machiNe     testing LeArNinG\" wallet";
        expectedPhrases = new String[][]{new String[]{"comput", "vision"}, new String[]{"machin", "test", "learn"}};
        expectedQuery = "test comput vision blanket machin test learn wallet";

        result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(result.getQuery(), expectedQuery.split(" "));
        Assertions.assertTrue(Arrays.deepEquals(result.getPhrases(), expectedPhrases));

        rawQuery = "testing     \"Computer, VISION\" !!!!!!blanket \"machiNe   !!!!  tesTing LeArNinG\" \"wallet";
        expectedPhrases = new String[][]{new String[]{"comput", "vision"}, new String[]{"machin", "test", "learn"}};
        expectedQuery = "test comput vision blanket machin test learn wallet";

        ProcessedQuery result1 = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(result1.getQuery(), expectedQuery.split(" "));
        Assertions.assertTrue(Arrays.deepEquals(result1.getPhrases(), expectedPhrases));
        Assertions.assertEquals(result, result1);

        rawQuery = "testing     \"VISION Computer, \" !!!!!!blanket \"machiNe   !!!!  tesTing LeArNinG\" \"wallet";
        expectedPhrases = new String[][]{new String[]{"vision", "comput"}, new String[]{"machin", "test", "learn"}};
        expectedQuery = "test comput vision blanket machin test learn wallet";

        ProcessedQuery result2 = TextProcessing.cleanRawQuery(rawQuery);
        Assertions.assertNotEquals(result, result2);

        rawQuery = "testing \",\" !!!!!!blanket \"   !!!!  \" \"wallet";
        expectedQuery = "test blanket wallet";

        result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(result.getQuery(), expectedQuery.split(" "));
        Assertions.assertNull(result.getPhrases());

        rawQuery = "testing     \"Computer, VISION\" !!!!!!blanket \"machiNe   !!!!  tesTing LeArNinG\" \"wallet";
        expectedPhrases = new String[][]{new String[]{"comput", "vision"}, new String[]{"machin", "test", "learn"}};
        expectedQuery = "test blanket wallet";

        result = TextProcessing.cleanRawQuery(rawQuery);

        Assertions.assertArrayEquals(result.getFilteredQuery(), expectedQuery.split(" "));
    }
}
