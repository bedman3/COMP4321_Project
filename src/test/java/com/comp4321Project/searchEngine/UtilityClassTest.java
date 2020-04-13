package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Util.UrlProcessing;
import org.junit.Assert;
import org.junit.Test;

public class UtilityClassTest {
    @Test
    public void testGetBaseUrl() {

        Assert.assertEquals(UrlProcessing.getBaseUrl("http://www.cse.ust.hk"), "www.cse.ust.hk");
        Assert.assertEquals(UrlProcessing.getBaseUrl("http://www.cse.ust.hk/"), "www.cse.ust.hk");
        Assert.assertEquals(UrlProcessing.getBaseUrl("www.cse.ust.hk"), "www.cse.ust.hk");
        Assert.assertEquals(UrlProcessing.getBaseUrl("https://www.cse.ust.hk"), "www.cse.ust.hk");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBaseUrlException() {
        UrlProcessing.getBaseUrl("fbcom");
    }

    @Test
    public void testTrimHeaderAndSlashAtTheEnd() {
        Assert.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("http://www.cse.ust.hk/"), "www.cse.ust.hk");
        Assert.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("http://www.cse.ust.hk////"), "www.cse.ust.hk");
        Assert.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("www.cse.ust.hk////"), "www.cse.ust.hk");
        Assert.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("www.cse.ust.hk/"), "www.cse.ust.hk");
        Assert.assertEquals(UrlProcessing.trimHeaderAndSlashAtTheEnd("www.cse.ust.hk"), "www.cse.ust.hk");
    }

    @Test
    public void testIsUrlEqual() {
        Assert.assertTrue(UrlProcessing.isUrlEqual("http://www.cse.ust.hk/", "http://www.cse.ust.hk"));
        Assert.assertTrue(UrlProcessing.isUrlEqual("http://www.cse.ust.hk", "http://www.cse.ust.hk"));
        Assert.assertTrue(UrlProcessing.isUrlEqual("www.cse.ust.hk/", "http://www.cse.ust.hk"));
        Assert.assertTrue(UrlProcessing.isUrlEqual("www.cse.ust.hk", "http://www.cse.ust.hk"));
    }
}
