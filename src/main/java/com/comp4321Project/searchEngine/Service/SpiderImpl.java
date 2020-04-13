package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Util.RocksDBUtil;
import com.comp4321Project.searchEngine.Util.TextProcessing;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;

public class SpiderImpl implements Spider {
    private final RocksDBDao rocksDBDao;
    private final Integer topKKeywords;
    private final static Character spaceSeparator = ' ';

    public SpiderImpl(RocksDBDao rocksDBDao, Integer topKKeywords) {
        this.rocksDBDao = rocksDBDao;
        this.topKKeywords = topKKeywords;
    }

    @Override
    public void scrape(String url) throws IOException, RocksDBException {
        RocksDB rocksDB = this.rocksDBDao.getRocksDB();

        Set<String> linksIdSet = new HashSet<String>();
        Set<String> linksStringSet = new HashSet<String>();

        Connection.Response response = Jsoup.connect(url).execute();
        // extract last modified from response header
        String lastModified = response.header("Last-Modified");
        if (lastModified == null) {
            lastModified = response.header("Date");
        }

        String size = response.header("Content-Length");

        // convert parent url to url id
        String parentUrlId = RocksDBUtil.getUrlIdFromUrl(rocksDBDao, url);

        Document doc = response.parse();
        Elements linkElements = doc.select("a[href]");
        String docText = doc.text();
        for (Element linkElement : linkElements) {
            String link = linkElement.attr("href");
            // it will contain links like /admin/qa/
            // we will need to append the parent url to the relative url
            // so the result will become http://www.cse.ust.hk/admin/qa/
            if (link.startsWith("/")) {
                link = url + link;
            } else if (link.startsWith("#")) {
                // ignore any links start with element link #
                continue;
            } else if (link.equals(url)) {
                // do not create self loop
                continue;
            } else {
                // if urls not under url, then we ignore the outgoing links
                // e.g. if url == http://www.cse.ust.hk, we will ignore
                // any link that does not start with http://www.cse.ust.hk
                continue;
            }
            linksStringSet.add(link);
            linksIdSet.add(RocksDBUtil.getUrlIdFromUrl(rocksDBDao, link));
        }

        // tokenize the words
        String parsedText = docText;

        // convert all text to lower case
        parsedText = parsedText.toLowerCase();

        // remove all punctuations
        parsedText = parsedText.replaceAll("\\p{P}", "");

        String[] wordsArray = StringUtils.split(parsedText, " ");

        Map<String, Integer> keyFreqMap = new HashMap<>();
        TextProcessing textProcessing = new TextProcessing();

        for (String word : wordsArray) {
            // remove/ignore stopwords when counting frequency
            if (textProcessing.isStopWord(word)) {
                continue;
            }

            String wordKey = RocksDBUtil.getWordIdFromWord(rocksDBDao, word);

            // increment frequency by 1
            keyFreqMap.merge(wordKey, 1, Integer::sum);
        }

        // build a max heap to get the top 5 key freq
        PriorityQueue<Map.Entry<String, Integer>> maxHeap = new PriorityQueue<>((p1, p2) -> {
            // compare in descending order
            return p1.getValue().compareTo(p2.getValue()) * -1;
        });

        keyFreqMap.forEach((String key, Integer value) -> maxHeap.add(new AbstractMap.SimpleEntry<>(key, value)));

        Iterator<Map.Entry<String, Integer>> keyFreqIt = maxHeap.iterator();
        StringBuilder keyFreqTopKValue = new StringBuilder();

        for (int index = 0; index < this.topKKeywords && keyFreqIt.hasNext(); index++) {
            Map.Entry<String, Integer> pair = keyFreqIt.next();
            try {
                String keyword = RocksDBUtil.getWordFromWordId(rocksDBDao, pair.getKey());
                keyFreqTopKValue.append(keyword);
                keyFreqTopKValue.append(" ");
                keyFreqTopKValue.append(pair.getValue());
                keyFreqTopKValue.append(";");
            } catch (NullPointerException e) {
                System.err.println(e.toString());
            }
        }

        // serialize the map and store it to rocksdb
        Gson gson = new Gson();
        String keyFreqJsonString = gson.toJson(keyFreqMap);
        rocksDB.put(rocksDBDao.getUrlIdToKeywordFrequencyRocksDBCol(), parentUrlId.getBytes(), keyFreqJsonString.getBytes());
        rocksDB.put(rocksDBDao.getUrlIdToTop5Keyword(), parentUrlId.getBytes(), keyFreqTopKValue.toString().getBytes());

        // store title
        String title = doc.title();

        // extract the size
        if (size == null) {
            // remove all whitespaces and get the length = number of characters
            int length = docText.replaceAll("\\s+", "").length();
            size = length + " characters";
        } else {
            size += " bytes";
        }

        // the spider will overwrite the key-value pair in rocksdb because parent -> child paths are
        // meant to be overwrote if there is update
        rocksDB.put(rocksDBDao.getParentUrlIdToChildUrlIdRocksDBCol(), parentUrlId.getBytes(), StringUtils.join(linksIdSet, spaceSeparator).getBytes());

        for (String linkId : linksIdSet) {
            byte[] childUrlIdToParentUrlIdValueByte = rocksDB.get(rocksDBDao.getChildUrlIdToParentUrlIdRocksDBCol(), linkId.getBytes());

            if (childUrlIdToParentUrlIdValueByte == null) {
                rocksDB.put(rocksDBDao.getChildUrlIdToParentUrlIdRocksDBCol(), linkId.getBytes(), parentUrlId.getBytes());
            } else {
                String childUrlIdToParentUrlIdValue = new String(childUrlIdToParentUrlIdValueByte);
                if (childUrlIdToParentUrlIdValue.contains(parentUrlId)) {
                    // do nothing
                } else {
                    // append in the end
                    childUrlIdToParentUrlIdValue += String.format("%c%s", spaceSeparator, parentUrlId);
                    rocksDB.put(rocksDBDao.getChildUrlIdToParentUrlIdRocksDBCol(), linkId.getBytes(), childUrlIdToParentUrlIdValue.getBytes());
                }
            }
        }

        // compute parent links list string
        String childLinksListString = Joiner.on('\n').join(linksStringSet);

        // use a separator that will rarely appear in the title
        String metaValue = new SiteMetaData(title, url, lastModified, size, -1.0, keyFreqTopKValue.toString(), childLinksListString).toMetaDataString();
        rocksDB.put(rocksDBDao.getUrlIdToMetaDataRocksDBCol(), parentUrlId.getBytes(), metaValue.getBytes());
    }

    @Override
    public void scrape(String url, Boolean recursive, Integer limit) throws IOException, RocksDBException {



        this.scrape(url);
    }

    @Override
    public void scrape(String url, Boolean recursive) throws IOException, RocksDBException {
        this.scrape(url, recursive, null);
    }

    public Integer getTopKKeywords() {
        return topKKeywords;
    }

    public static Character getSpaceSeparator() {
        return spaceSeparator;
    }
}
