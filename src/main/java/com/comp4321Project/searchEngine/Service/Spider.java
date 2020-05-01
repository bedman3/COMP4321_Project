package com.comp4321Project.searchEngine.Service;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Model.Constants;
import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Util.TextProcessing;
import com.comp4321Project.searchEngine.Util.UrlProcessing;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class Spider {
    private final static Character spaceSeparator = ' ';
    private final RocksDBDao rocksDBDao;
    private final Integer topKKeywords;

    public Spider(RocksDBDao rocksDBDao) {
        this.rocksDBDao = rocksDBDao;
        this.topKKeywords = Constants.getExtractTopKKeywords();
    }

    public static Character getSpaceSeparator() {
        return spaceSeparator;
    }

    /**
     * @param rawUrl the url you want to crawl
     * @return a set of child url
     * @throws IOException
     * @throws RocksDBException
     */
    public Set<String> crawlOneSite(String rawUrl) throws IOException, RocksDBException {
        RocksDB rocksDB = this.rocksDBDao.getRocksDB();
        InvertedFile invertedFileForBody = this.rocksDBDao.getInvertedFileForBody();
        InvertedFile invertedFileForTitle = this.rocksDBDao.getInvertedFileForTitle();

        String url = UrlProcessing.trimHeaderAndSlashAtTheEnd(rawUrl);
        String baseUrl = UrlProcessing.getBaseUrl(url);
        String httpUrl = String.format("http://%s", url);

        Set<String> linksIdSet = new HashSet<String>();
        Set<String> linksStringSet = new HashSet<String>();

        Connection.Response response;
        try {
            response = Jsoup.connect(httpUrl)
                    .timeout(1000 * Constants.getConnectionTimeout())
                    .execute();
        } catch (HttpStatusException e) {
            System.err.println(e.toString());
            return null;
        }
        // extract last modified from response header
        String lastModified = response.header("Last-Modified");
        if (lastModified == null) {
            lastModified = response.header("Date");
        }

        // convert parent url to url id
        String parentUrlId = rocksDBDao.getUrlIdFromUrl(url);

        // check last modification date to determine whether or not to ignore this url,
        // update the latest modified date accordingly
        if (rocksDBDao.isIgnoreWithLastModifiedDate(parentUrlId, lastModified)) {
            System.err.println("skip crawling " + rawUrl + ", parameters -> url: " + url + " lastModified: " + lastModified + " urlId: " + parentUrlId);
            return null;
        }

        Document doc = response.parse();
        Elements linkElements = doc.select("a[href]");
        for (Element linkElement : linkElements) {
            String link = linkElement.attr("href");
            // it will contain links like /admin/qa/
            // we will need to append the parent url to the relative url
            // so the result will become http://www.cse.ust.hk/admin/qa/

            try {
                if (link.startsWith("/") && !UrlProcessing.containsOtherFileType(link)) {
                    link = UrlProcessing.trimHeaderAndSlashAtTheEnd(baseUrl + link);
                } else if (link.contains("javascript:")) {
                    // ignore javascript link
                    continue;
                } else if (link.startsWith("#")) {
                    // ignore any links start with element link #
                    continue;
                } else if (link.startsWith("?")) {
                    // ignore query link
                    continue;
                } else if (!link.startsWith("/") && !link.contains(".") &&
                        !link.contains("://") && !UrlProcessing.containsOtherFileType(link)) {
                    link = url + "/" + link;
                } else {
                    // if urls not under url, then we ignore the outgoing links
                    // e.g. if url == http://www.cse.ust.hk, we will ignore
                    // any link that does not start with http://www.cse.ust.hk
                    continue;
                }

                if (UrlProcessing.isUrlEqual(link, url)) {
                    // do not create self loop
                    continue;
                }

                linksStringSet.add(link);
                linksIdSet.add(rocksDBDao.getUrlIdFromUrl(link));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        // store title
        String title = doc.title();

        // stem words here using Porter's algorithm
        String[] wordsBodyArray, wordsTitleArray;
        Element docBody = doc.body();
        if (docBody != null) {
            wordsBodyArray = TextProcessing.cleanRawWords(docBody.text());
        } else {
            wordsBodyArray = new String[]{};
        }
        wordsTitleArray = TextProcessing.cleanRawWords(doc.title());

        Map<String, Integer> keyFreqMap = new HashMap<>();
        Map<String, Double> keyTermFreqMap = new HashMap<>();

        for (int index = 0; index < wordsBodyArray.length; index++) {
            String word = wordsBodyArray[index];
            String wordId = rocksDBDao.getWordIdFromWord(word);

            // increment frequency by 1
            keyFreqMap.merge(wordId, 1, Integer::sum);
            invertedFileForBody.add(wordId, parentUrlId, index, true);
        }

        for (int index = 0; index < wordsTitleArray.length; index++) {
            String word = wordsTitleArray[index];
            String wordId = rocksDBDao.getWordIdFromWord(word);
            invertedFileForTitle.add(wordId, parentUrlId, index, true);
        }

        // build a max heap to get the top 5 key freq
        PriorityQueue<Map.Entry<String, Integer>> maxHeap = new PriorityQueue<>((p1, p2) -> {
            // compare in descending order
            return p1.getValue().compareTo(p2.getValue()) * -1;
        });

        keyFreqMap.forEach((String key, Integer value) -> maxHeap.add(new AbstractMap.SimpleEntry<>(key, value)));

        Iterator<Map.Entry<String, Integer>> keyFreqIt = maxHeap.iterator();

        Integer freqOfMostFrequentTermInDoc;
        if (maxHeap.size() == 0) {
            freqOfMostFrequentTermInDoc = 0;
        } else {
            freqOfMostFrequentTermInDoc = maxHeap.peek().getValue();
        }

        // calculate the term frequency for TD-IDF, normalized by the frequency of the most frequent term in document
        for (Map.Entry<String, Integer> entry : keyFreqMap.entrySet()) {
            keyTermFreqMap.put(entry.getKey(), entry.getValue() * 1.0 / freqOfMostFrequentTermInDoc);
        }

        StringBuilder keyFreqTopKValue = new StringBuilder();

        for (int index = 0; index < this.topKKeywords && keyFreqIt.hasNext(); index++) {
            Map.Entry<String, Integer> pair = keyFreqIt.next();
            try {
                String keyword = rocksDBDao.getWordFromWordId(pair.getKey());
                keyFreqTopKValue.append(keyword);
                keyFreqTopKValue.append(" ");
                keyFreqTopKValue.append(pair.getValue());
                keyFreqTopKValue.append(";");
            } catch (NullPointerException e) {
                System.err.println(e.toString());
            }
        }

        // serialize the map and store it to rocksdb
        rocksDBDao.putKeywordFrequencyData(parentUrlId, keyFreqMap);
        rocksDBDao.putKeywordTermFrequencyData(parentUrlId, keyTermFreqMap);
        rocksDBDao.putTop5KeywordData(parentUrlId, keyFreqTopKValue);

        String size = response.header("Content-Length");

        // extract the size
        if (size == null) {
            // remove all whitespaces and get the length = number of characters
            int length = doc.body().text().replaceAll("\\s+", "").length();
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

        return linksStringSet;
    }

    public void crawl(String url, Boolean recursive, Integer limit) throws IOException, RocksDBException {
        if (limit != null && limit <= 0) {
            throw new IllegalArgumentException("limit should be greater than 0");
        }

        if (!recursive) {
            this.crawlOneSite(url);
        } else {
            // recursive crawl
            Set<String> crawledSite = new HashSet<>();
            Queue<String> crawlQueue = new LinkedList<>();
            Set<String> returnSet;
            int updateInvertedFileInterval = Constants.getInvertedFileUpdateInterval();
            Integer numScrapedSite = 1;

            crawlQueue.add(url);
            // BFS for scraping website
            while (crawlQueue.peek() != null && (limit == null || numScrapedSite <= limit)) {
                String crawlUrl = crawlQueue.poll();
                System.err.println("Scraping: " + crawlUrl + ", scraped " + numScrapedSite.toString() + " site(s).");
                returnSet = this.crawlOneSite(crawlUrl);
                crawledSite.add(crawlUrl);

                if (returnSet != null) {
                    // if crawlUrl is not ignored, which means site is crawled
                    for (String childUrl : returnSet) {
                        if (!crawledSite.contains(childUrl)) {
                            crawlQueue.add(childUrl);
                        }
                    }
                }

                // for each interval, update the invertedfile
                if (numScrapedSite % updateInvertedFileInterval == 0) {
                    rocksDBDao.updateInvertedFileInRocksDB();
                }

                numScrapedSite++;
            }
        }
        rocksDBDao.updateInvertedFileInRocksDB();
    }

    public void crawl(String url, Boolean recursive) throws IOException, RocksDBException {
        this.crawl(url, recursive, null);
    }

    public void crawl(String url) throws IOException, RocksDBException {
        this.crawlOneSite(url);
    }

    public Integer getTopKKeywords() {
        return topKKeywords;
    }
}
