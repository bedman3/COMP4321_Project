package com.comp4321Project.searchEngine;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

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
				}
				links.add(link);
			}

			for (String link : links) {
				System.out.println(link);
			}

//			rocksDB.put(url.getBytes(), links);

		} catch (RocksDBException e) {
			System.err.println(e.toString());
		}
	}

}
