package com.comp4321Project.searchEngine;

import org.apache.tomcat.util.buf.StringUtils;
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
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

//	public static void main(String[] args) {
//		SpringApplication.run(Main.class, args);
//	}

	// my playground function for doing some RAD
	public static void main(String[] arg) throws IOException {
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

			Document doc = Jsoup.connect(url).get();
			Elements elements = doc.select("a[href]");
			for (Element element : elements) {
				String link = element.attr("href");
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



			// index child links, index in the format of: key = child_{parent_link}, value = {child_link}
			// the spider will overwrite the key-value pair in rocksdb because parent -> child paths are
			// meant to be overwrote if there is update
			String childLinkKey = String.format("child_%s", url);
			byte[] test = rocksDBSiteMapData.get(childLinkKey.getBytes());
			if (test != null) {
				System.out.println(new String(test));
			} else {
				System.out.println(test);
			}
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
