package com.comp4321Project.searchEngine.Util;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteOptions;

public class RocksDBUtil {
    final static String nextAvailableIdLiteral = "nextAvailableId";
    final static String urlToIdKeyPrefix = "urlToId_";
    final static String wordToIdKeyPrefix = "wordToId_";
    final static String idToUrlKeyPrefix = "idToUrl_";
    final static String idToWordKeyPrefix = "idToWord_";

    public static void initRocksDBWithNextAvailableId(RocksDB rocksDB, ColumnFamilyHandle colHandle) throws RocksDBException {
        System.out.println("nextavailableid colHandle: " + new String(colHandle.getName()));

        byte[] nextAvailableIdByte = rocksDB.get(colHandle, nextAvailableIdLiteral.getBytes());

        if (nextAvailableIdByte == null) {
            // if next available id field does not exist, assumes there is no document in it and we start from 0
            rocksDB.put(colHandle, new WriteOptions().setSync(true), nextAvailableIdLiteral.getBytes(), "0".getBytes());
        }
    }

    public static String getUrlIdFromUrl(RocksDB rocksDB, ColumnFamilyHandle colHandle, String url) throws RocksDBException {
        return getIdMerge(rocksDB, colHandle, urlToIdKeyPrefix, url);
    }

    public static String getWordIdFromWord(RocksDB rocksDB, ColumnFamilyHandle colHandle, String word) throws RocksDBException {
        return getIdMerge(rocksDB, colHandle, wordToIdKeyPrefix, word);
    }

    /*
    This function will check if the key exists in rocksdb, if not it will create one
     */
    public static String getIdMerge(RocksDB rocksDB, ColumnFamilyHandle colHandle, String keyPrefix, String key) throws RocksDBException {
        String idKey = String.format("%s%s", keyPrefix, key);
        byte[] urlIdByte = rocksDB.get(colHandle, idKey.getBytes());
        if (urlIdByte == null) {
            // TODO: use merge mechanism in rocksdb to ensure the nextAvailableId is synchronized
            //  when running with multiple workers

            // get the next available id
            byte[] nextAvailableIdStringByte = rocksDB.get(colHandle, nextAvailableIdLiteral.getBytes());
            Integer nextAvailableId = Integer.parseInt(new String(nextAvailableIdStringByte));
            rocksDB.put(colHandle, nextAvailableIdLiteral.getBytes(), (new Integer(nextAvailableId + 1)).toString().getBytes());

            // register this url with the new unique id
            rocksDB.put(colHandle, idKey.getBytes(), nextAvailableId.toString().getBytes());

            return nextAvailableId.toString();
        } else {
            return new String(urlIdByte);
        }
    }
}
