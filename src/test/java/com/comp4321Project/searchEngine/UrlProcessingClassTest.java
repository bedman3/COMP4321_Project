package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Util.UrlProcessing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UrlProcessingClassTest {
    @Test
    public void testGetBaseUrl() {

        Assertions.assertEquals(UrlProcessing.getBaseUrl("http://www.cse.ust.hk"), "www.cse.ust.hk");
        Assertions.assertEquals(UrlProcessing.getBaseUrl("http://www.cse.ust.hk/"), "www.cse.ust.hk");
        Assertions.assertEquals(UrlProcessing.getBaseUrl("www.cse.ust.hk"), "www.cse.ust.hk");
        Assertions.assertEquals(UrlProcessing.getBaseUrl("https://www.cse.ust.hk"), "www.cse.ust.hk");
    }

    @Test
    public void testGetBaseUrlException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            UrlProcessing.getBaseUrl("fbcom");
        });
    }

    @Test
    public void testTrimHeaderAndSlashAtTheEnd() {
        Assertions.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("http://www.cse.ust.hk/"), "www.cse.ust.hk");
        Assertions.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("http://www.cse.ust.hk////"), "www.cse.ust.hk");
        Assertions.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("www.cse.ust.hk////"), "www.cse.ust.hk");
        Assertions.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("www.cse.ust.hk/"), "www.cse.ust.hk");
        Assertions.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("www.cse.ust.hk"), "www.cse.ust.hk");
    }

    @Test
    public void testIsUrlEqual() {
        Assertions.assertTrue(UrlProcessing.isUrlEqual("http://www.cse.ust.hk/", "http://www.cse.ust.hk"));
        Assertions.assertTrue(UrlProcessing.isUrlEqual("http://www.cse.ust.hk", "http://www.cse.ust.hk"));
        Assertions.assertTrue(UrlProcessing.isUrlEqual("www.cse.ust.hk/", "http://www.cse.ust.hk"));
        Assertions.assertTrue(UrlProcessing.isUrlEqual("www.cse.ust.hk", "http://www.cse.ust.hk"));
    }

    @Test
    public void testUrlContainOtherFileType() {
        Assertions.assertFalse(UrlProcessing.containsOtherFileType("/"));
        Assertions.assertFalse(UrlProcessing.containsOtherFileType("/index.html"));
        Assertions.assertFalse(UrlProcessing.containsOtherFileType(""));
        Assertions.assertTrue(UrlProcessing.containsOtherFileType("/index.jpg"));
        Assertions.assertTrue(UrlProcessing.containsOtherFileType("/index.png"));
    }

    @Test
    public void testCleanContentAfterFileType() {
        Assertions.assertEquals("www.cse.ust.hk/ct/fyp/getting_started.html", UrlProcessing.cleanContentAfterFileType("www.cse.ust.hk/ct/fyp/getting_started.html/reports/"));
        Assertions.assertEquals("www.cse.ust.hk/ct/fyp/getting_started.html", UrlProcessing.cleanContentAfterFileType("www.cse.ust.hk/ct/fyp/getting_started.html/content"));
        Assertions.assertEquals("www.cse.ust.hk/ct/fyp/getting_started.html", UrlProcessing.cleanContentAfterFileType("www.cse.ust.hk/ct/fyp/getting_started.html/content/content"));
        Assertions.assertEquals("www.cse.ust.hk/ct/fyp/getting_started.html", UrlProcessing.cleanContentAfterFileType("www.cse.ust.hk/ct/fyp/getting_started.html/content/content/content"));
        Assertions.assertEquals("www.cse.ust.hk/ct/fyp/getting_started.html", UrlProcessing.cleanContentAfterFileType("www.cse.ust.hk/ct/fyp/getting_started.html#haha"));
        Assertions.assertEquals("www.cse.ust.hk/ct/fyp/getting_started.html", UrlProcessing.cleanContentAfterFileType("www.cse.ust.hk/ct/fyp/getting_started.html?v2"));
    }
}
