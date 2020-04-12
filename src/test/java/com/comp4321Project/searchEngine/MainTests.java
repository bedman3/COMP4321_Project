package com.comp4321Project.searchEngine;

import com.comp4321Project.searchEngine.Util.Util;
import org.junit.jupiter.api.Test;
import org.rocksdb.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class MainTests {

	@Test
	void contextLoads() {
	}

	@Test
	void playGround() {
		Util.createDirectoryIfNotExist("rocksDbFiles/UrlIdData/");
	}

	@Test
	void test1() {
		try {
			Util.createDirectoryIfNotExist("testFiles");
			DBOptions dbOptions = new DBOptions();
			dbOptions.setCreateIfMissing(true);
			dbOptions.setCreateMissingColumnFamilies(true);

//			ColumnFamilyDescriptor urlIdDataColumn = ;
//			ColumnFamilyDescriptor wordIdDataColumn = ;

			List<ColumnFamilyDescriptor> columnFamilyDescriptorList = Arrays.asList(
					new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY),
					new ColumnFamilyDescriptor("urlIdData".getBytes()),
					new ColumnFamilyDescriptor("wordIdData".getBytes())
			);
			List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();


			RocksDB rocksDB = RocksDB.open(dbOptions, "testFiles", columnFamilyDescriptorList, columnFamilyHandleList);

//			rocksDB.createColumnFamily(urlIdDataColumn);
//			rocksDB.createColumnFamily(wordIdDataColumn);

			columnFamilyHandleList.forEach(x -> {
				try {
					System.out.println(new String(x.getName()));
				} catch (RocksDBException e) {
					e.printStackTrace();
				}
			});

			rocksDB.put(columnFamilyHandleList.get(0), "test0".getBytes(), "0".getBytes());
			rocksDB.put(columnFamilyHandleList.get(1), "test1".getBytes(), "1".getBytes());
			rocksDB.put(columnFamilyHandleList.get(2), "test2".getBytes(), "2".getBytes());

			RocksIterator rocksIterator = rocksDB.newIterator(columnFamilyHandleList.get(1));
			for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
				System.out.println("key: " + new String(rocksIterator.key()) + " | value: " + new String(rocksIterator.value()));
			}

//			System.out.println();
//			rocksDB.put(columnFamilyHandleList[0]);


		} catch (RocksDBException e) {
			System.err.println(e.toString());
		}

	}

}
