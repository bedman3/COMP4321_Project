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
        byte[] nextAvailableIdByte = rocksDB.get(colHandle, nextAvailableIdLiteral.getBytes());

        if (nextAvailableIdByte == null) {
            // if next available id field does not exist, assumes there is no document in it and we start from 0
            rocksDB.put(colHandle, new WriteOptions().setSync(true), nextAvailableIdLiteral.getBytes(), "0".getBytes());
        }
    }

    public static String getUrlIdFromUrl(RocksDB rocksDB, ColumnFamilyHandle colHandle, String url) throws RocksDBException {
        return getIdFromKey(rocksDB, colHandle, urlToIdKeyPrefix, idToUrlKeyPrefix, url);
    }

    public static String getWordIdFromWord(RocksDB rocksDB, ColumnFamilyHandle colHandle, String word) throws RocksDBException {
        return getIdFromKey(rocksDB, colHandle, wordToIdKeyPrefix, idToWordKeyPrefix, word);
    }

    public static String getUrlFromUrlId(RocksDB rocksDB, ColumnFamilyHandle colHandle, String urlId) throws RocksDBException {
        return getKeyFromId(rocksDB, colHandle, idToUrlKeyPrefix, urlId);
    }

    public static String getWordFromWordId(RocksDB rocksDB, ColumnFamilyHandle colHandle, String wordId) throws RocksDBException {
        return getKeyFromId(rocksDB, colHandle, idToWordKeyPrefix, wordId);
    }

    /**
     * This function will check if the key exists in rocksdb, if not it will create one
     * @param rocksDB
     * @param colHandle
     * @param keyPrefix
     * @param reverseKeyPrefix
     * @param key
     * @return
     * @throws RocksDBException
     */
    public static String getIdFromKey(RocksDB rocksDB, ColumnFamilyHandle colHandle, String keyPrefix, String reverseKeyPrefix, String key) throws RocksDBException {
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

            // register a reverse index
            String reverseIdKey = String.format("%s%s", reverseKeyPrefix, nextAvailableId);
            System.out.println(reverseIdKey);
            rocksDB.put(colHandle, reverseIdKey.getBytes(), key.getBytes());

            return nextAvailableId.toString();
        } else {
            return new String(urlIdByte);
        }
    }

    /**
     * This function assumes the required key has already ran through {@link #getIdFromKey(RocksDB, ColumnFamilyHandle, String, String, String)}
     * @param rocksDB
     * @param colHandle
     * @return
     */
    public static String getKeyFromId(RocksDB rocksDB, ColumnFamilyHandle colHandle, String reversePrefix, String id) throws RocksDBException {
        String reverseIdKey = String.format("%s%s", reversePrefix, id);
        System.out.println(reverseIdKey);
        return new String(rocksDB.get(colHandle, reverseIdKey.getBytes()));
    }
}
