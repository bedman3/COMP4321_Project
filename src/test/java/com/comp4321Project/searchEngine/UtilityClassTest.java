package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Util.UrlProcessing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilityClassTest {
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
}
