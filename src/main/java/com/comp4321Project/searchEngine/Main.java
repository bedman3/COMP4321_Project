package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Util.RocksDBUtil;
import com.comp4321Project.searchEngine.Util.Util;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

//	public static void main(String[] args) {
//		SpringApplication.run(Main.class, args);
//	}

    // my playground function for doing some RAD
    public static void main(String[] arg) throws IOException {
        String[] stopWords = {
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
        Set<String> stopWordsSet = new HashSet<String>(Arrays.asList(stopWords));

        char separator = ' ';
        final String rocksDBDirectory = "rocksDBFiles";
        final Integer extractTopKKeywords = 5;

        Util.createDirectoryIfNotExist(rocksDBDirectory);

        RocksDB.loadLibrary();

        try {
            // The Options class contains a set of configurable DB options
            // that determines the behaviour of the database.
            DBOptions dbOptions = new DBOptions();
            dbOptions.setCreateIfMissing(true);
            dbOptions.setCreateMissingColumnFamilies(true);

            List<ColumnFamilyDescriptor> columnFamilyDescriptorList = Arrays.asList(
                    new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY),
                    new ColumnFamilyDescriptor("WebsiteMetaData".getBytes()),
                    new ColumnFamilyDescriptor("VSpaceModelIndexData".getBytes()),
                    new ColumnFamilyDescriptor("SiteMapData".getBytes()),
                    new ColumnFamilyDescriptor("urlIdData".getBytes()),
                    new ColumnFamilyDescriptor("wordIdData".getBytes()),
                    new ColumnFamilyDescriptor("keywordFrequencyData".getBytes())
            );
            List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();

            RocksDB rocksDB = RocksDB.open(dbOptions, rocksDBDirectory, columnFamilyDescriptorList, columnFamilyHandleList);

            ColumnFamilyHandle defaultRocksDBCol = columnFamilyHandleList.get(0);
            ColumnFamilyHandle websiteMetaDataRocksDBCol = columnFamilyHandleList.get(1);
            ColumnFamilyHandle vSpaceModelIndexDataRocksDBCol = columnFamilyHandleList.get(2);
            ColumnFamilyHandle siteMapDataRocksDBCol = columnFamilyHandleList.get(3);
            ColumnFamilyHandle urlIdDataRocksDBCol = columnFamilyHandleList.get(4);
            ColumnFamilyHandle wordIdDataRocksDBCol = columnFamilyHandleList.get(5);
            ColumnFamilyHandle keywordFrequencyDataRocksDBCol = columnFamilyHandleList.get(6);


            // init rocksdb for id data
            RocksDBUtil.initRocksDBWithNextAvailableId(rocksDB, urlIdDataRocksDBCol);
            RocksDBUtil.initRocksDBWithNextAvailableId(rocksDB, wordIdDataRocksDBCol);

            String url = "http://www.cse.ust.hk";
            Set<String> linksIdSet = new HashSet<String>();

            Connection.Response response = Jsoup.connect(url).execute();
            // extract last modified from response header
            String lastModified = response.header("Last-Modified");
            if (lastModified == null) {
                lastModified = response.header("Date");
            }

            String size = response.header("Content-Length");

            // convert parent url to url id
            String parentUrlId = RocksDBUtil.getUrlIdFromUrl(rocksDB, urlIdDataRocksDBCol, url);

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
                linksIdSet.add(RocksDBUtil.getUrlIdFromUrl(rocksDB, urlIdDataRocksDBCol, link));
            }

            // tokenize the words
            String parsedText = docText;

            // convert all text to lower case
            parsedText = parsedText.toLowerCase();

            // remove all punctuations
            parsedText = parsedText.replaceAll("\\p{P}", "");

            String[] wordsArray = StringUtils.split(parsedText, " ");

            Map<String, Integer> keyFreqMap = new HashMap<>();

            for (String word : wordsArray) {
                // remove/ignore stopwords when counting frequency
                if (stopWordsSet.contains(word)) {
                    continue;
                }

                String wordKey = RocksDBUtil.getWordIdFromWord(rocksDB, wordIdDataRocksDBCol, word);

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
//            List<String> topKKeywordsIdList = new ArrayList<>();
//            List<Integer> topKFrequencyList = new ArrayList<>();

            for (int index = 0; index < extractTopKKeywords && keyFreqIt.hasNext(); index++) {
                Map.Entry<String, Integer> pair = keyFreqIt.next();
                try {
                    String keyword = RocksDBUtil.getWordFromWordId(rocksDB, wordIdDataRocksDBCol, pair.getKey());
                    keyFreqTopKValue.append(keyword);
                    keyFreqTopKValue.append(" ");
                    keyFreqTopKValue.append(pair.getValue());
                    keyFreqTopKValue.append(";");
                } catch (NullPointerException e) {
                    System.err.println(e.toString());
                }
            }
//            topKKeywordsIdList.forEach(System.out::println);
//            List<byte[]> topKKeywordByteList = rocksDB.multiGetAsList(Arrays.asList(), topKKeywordsIdList.stream().map(String::getBytes).collect(Collectors.toList()));
//            System.out.println("testing:::");
//            topKKeywordByteList.forEach(System.out::println);
//            List<String> topKKeywordList = topKKeywordByteList.stream().map(String::new).collect(Collectors.toList());
//            for (int index = 0; index < topKKeywordsIdList.size(); index++) {
//                keyFreqTopKValue.append(topKKeywordList.get(index)).append(" ").append(topKFrequencyList.get(index)).append(";");
//            }

            // serialize the map and store it to rocksdb
            Gson gson = new Gson();
            String keyFreqJsonString = gson.toJson(keyFreqMap);
            String keyFreqTopKKeyPrefix = "topK_";
            String keyFreqTopKKey = String.format("%s%s", keyFreqTopKKeyPrefix, parentUrlId);
            rocksDB.put(keywordFrequencyDataRocksDBCol, parentUrlId.getBytes(), keyFreqJsonString.getBytes());
            rocksDB.put(keywordFrequencyDataRocksDBCol, keyFreqTopKKey.getBytes(), keyFreqTopKValue.toString().getBytes());

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

            String metaKey = String.format("meta_%s", url);
            // use a separator that will rarely appear in the title
			String metaValue = String.format("%s |,.| %s |,.| %s |,.| %s", title, url, lastModified, size);
			rocksDB.put(websiteMetaDataRocksDBCol, metaKey.getBytes(), metaValue.getBytes());


            // index child links, index in the format of: key = child_{parent_link}, value = {child_link}
            // the spider will overwrite the key-value pair in rocksdb because parent -> child paths are
            // meant to be overwrote if there is update
            String childLinkKey = String.format("child_%s", parentUrlId);
            rocksDB.put(siteMapDataRocksDBCol, childLinkKey.getBytes(), StringUtils.join(linksIdSet, separator).getBytes());

            // index parent links, index in the format of: key = parent_{child_link}, value = {parent_link}
            for (String link : linksIdSet) {
                String parentLinkKey = String.format("parent_%s", link);
                byte[] parentLinkValueByte = rocksDB.get(siteMapDataRocksDBCol, parentLinkKey.getBytes());

                if (parentLinkValueByte == null) {
                    rocksDB.put(siteMapDataRocksDBCol, parentLinkKey.getBytes(), parentUrlId.getBytes());
                } else {
                    String parentLinkValue = new String(parentLinkValueByte);
                    if (parentLinkValue.contains(parentUrlId)) {
                        // do nothing
                    } else {
                        // append in the end
                        parentLinkValue += String.format("%c%s", separator, parentUrlId);
                        rocksDB.put(siteMapDataRocksDBCol, parentLinkKey.getBytes(), parentLinkValue.getBytes());
                    }
                }
            }

            // print all storage
            for (ColumnFamilyHandle col : columnFamilyHandleList) {
                RocksIterator it = rocksDB.newIterator(col);

                System.out.println("ColumnFamily: " + new String(col.getName()));

                for (it.seekToFirst(); it.isValid(); it.next()) {
                    System.out.println("key: " + new String(it.key()) + " | value: " + new String(it.value()));
                }

                System.out.println();
            }
        } catch (RocksDBException e) {
            System.err.println(e.toString());
        }
    }

}
