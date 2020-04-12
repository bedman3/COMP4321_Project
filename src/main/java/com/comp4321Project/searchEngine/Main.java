package com.comp4321Project.searchEngine;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.*;

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
        Map<String, Integer> keyFreqMap = new HashMap<String, Integer>();

        char separator = ' ';

//		String dbPath = "rocksdbFiles";
        String websiteMetaDbPath = "rocksDbFiles/WebsiteMetaData/";
        String vSpaceModelIndexDbPath = "rocksDbFiles/VSpaceModelIndexData/";
        String siteMapDbPath = "rocksDbFiles/SiteMapData";

        RocksDB.loadLibrary();

        try {
            // The Options class contains a set of configurable DB options
            // that determines the behaviour of the database.
            Options options = new Options();
            options.setCreateIfMissing(true);

            RocksDB rocksDBWebsiteMetaData = RocksDB.open(options, websiteMetaDbPath);
            RocksDB rocksDBVSpaceModelIndexData = RocksDB.open(options, vSpaceModelIndexDbPath);
            RocksDB rocksDBSiteMapData = RocksDB.open(options, siteMapDbPath);

            String url = "http://www.cse.ust.hk";
            Set<String> links = new HashSet<String>();

            Connection.Response response = Jsoup.connect(url).execute();
            // extract last modified from response header
            String lastModified = response.header("Last-Modified");
            if (lastModified == null) {
                lastModified = response.header("Date");
            }

            String size = response.header("Content-Length");

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
                links.add(link);
            }

            // tokenize the words
            String parsedText = docText;

            // convert all text to lower case
            parsedText = parsedText.toLowerCase();

            // remove all punctuations
            parsedText = parsedText.replaceAll("\\p{P}", "");


            String[] wordsArray = StringUtils.split(parsedText, " ");

            for (String word : wordsArray) {
                // remove/ignore stopwords when counting frequency
                if (stopWordsSet.contains(word)) {
                    continue;
                }

                // increment frequency by 1
                keyFreqMap.merge(word, 1, Integer::sum);
            }

            keyFreqMap.forEach((String word, Integer freq) -> System.out.println("word: " + word + " freq: " + freq.toString()));

            // store title
            String title = doc.title();

            // extract the size
            if (size == null) {
                // remove all whitespaces and get the length = number of characters
                int length = docText.replaceAll("\\s+", "").length();
                size = Integer.toString(length) + "characters";
            } else {
                size += "bytes";
            }

            String metaKey = String.format("meta_%s", url);
            // use a separator that will rarely appear in the title
//			String metaValue = String.format("%s |,.| %s |,.| %s |,.| %s |,.| %s |,.| %s |,.| %s", title, url, lastModified, size, keyFreq, parentLinks, childLinks)
//			rocksDBWebsiteMetaData.put(titleKey.getBytes(), title.getBytes());


            // index child links, index in the format of: key = child_{parent_link}, value = {child_link}
            // the spider will overwrite the key-value pair in rocksdb because parent -> child paths are
            // meant to be overwrote if there is update
            String childLinkKey = String.format("child_%s", url);
            rocksDBSiteMapData.put(childLinkKey.getBytes(), StringUtils.join(links, separator).getBytes());

            // index parent links, index in the format of: key = parent_{child_link}, value = {parent_link}
            for (String link : links) {
                String parentLinkKey = String.format("parent_%s", link);
                byte[] parentLinkValueByte = rocksDBSiteMapData.get(parentLinkKey.getBytes());

                if (parentLinkValueByte == null) {
                    rocksDBSiteMapData.put(parentLinkKey.getBytes(), url.getBytes());
                } else {
                    String parentLinkValue = new String(parentLinkValueByte);
                    if (parentLinkValue.contains(url)) {
                        // do nothing
                    } else {
                        // append in the end
                        parentLinkValue += String.format("%c%s", separator, url);
                        rocksDBSiteMapData.put(parentLinkKey.getBytes(), parentLinkValue.getBytes());
                    }
                }
            }
        } catch (RocksDBException e) {
            System.err.println(e.toString());
        }
    }

}
