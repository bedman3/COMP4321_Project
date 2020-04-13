package com.comp4321Project.searchEngine.Util;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteOptions;

public class RocksDBUtil {

    public static void initRocksDBWithNextAvailableId(RocksDB rocksDB, ColumnFamilyHandle colHandle) throws RocksDBException {
        byte[] nextAvailableIdByte = rocksDB.get(colHandle, RocksDBColIndex.getNextAvailableIdLiteral().getBytes());

        if (nextAvailableIdByte == null) {
            // if next available id field does not exist, assumes there is no document in it and we start from 0
            rocksDB.put(colHandle, new WriteOptions().setSync(true), RocksDBColIndex.getNextAvailableIdLiteral().getBytes(), "0".getBytes());
        }
    }

    public static String getUrlIdFromUrl(RocksDBDao rocksDBDao, String url) throws RocksDBException {
        return getIdFromKey(rocksDBDao.getRocksDB(), rocksDBDao.getUrlToUrlIdRocksDBCol(), rocksDBDao.getUrlIdToUrlRocksDBCol(), url);
    }

    public static String getWordIdFromWord(RocksDBDao rocksDBDao, String word) throws RocksDBException {
        return getIdFromKey(rocksDBDao.getRocksDB(), rocksDBDao.getWordToWordIdRocksDBCol(), rocksDBDao.getWordIdToWordRocksDBCol(), word);
    }

    public static String getUrlFromUrlId(RocksDBDao rocksDBDao, String urlId) throws RocksDBException {
        return getKeyFromId(rocksDBDao.getRocksDB(), rocksDBDao.getUrlIdToUrlRocksDBCol(), urlId);
    }

    public static String getWordFromWordId(RocksDBDao rocksDBDao, String wordId) throws RocksDBException {
        return getKeyFromId(rocksDBDao.getRocksDB(), rocksDBDao.getWordIdToWordRocksDBCol(), wordId);
    }

    /**
     * This function will check if the key exists in rocksdb, if not it will create one
     *
     * @param rocksDB
     * @param keyToIdColHandle
     * @param idToKeyColHandle
     * @param key
     * @return
     * @throws RocksDBException
     */
    public static String getIdFromKey(RocksDB rocksDB, ColumnFamilyHandle keyToIdColHandle, ColumnFamilyHandle idToKeyColHandle, String key) throws RocksDBException {
        byte[] idByte = rocksDB.get(keyToIdColHandle, key.getBytes());
        if (idByte == null) {
            // TODO: use merge mechanism in rocksdb to ensure the nextAvailableId is synchronized
            //  when running with multiple workers

            // get the next available id
            byte[] nextAvailableIdStringByte = rocksDB.get(idToKeyColHandle, RocksDBColIndex.getNextAvailableIdLiteral().getBytes());
            Integer nextAvailableId = Integer.parseInt(new String(nextAvailableIdStringByte));
            rocksDB.put(idToKeyColHandle, RocksDBColIndex.getNextAvailableIdLiteral().getBytes(), (new Integer(nextAvailableId + 1)).toString().getBytes());

            // register this url with the new unique id
            rocksDB.put(keyToIdColHandle, key.getBytes(), nextAvailableId.toString().getBytes());

            // register a reverse index
            rocksDB.put(idToKeyColHandle, nextAvailableId.toString().getBytes(), key.getBytes());

            return nextAvailableId.toString();
        } else {
            return new String(idByte);
        }
    }

    /**
     * This function assumes the required key has already ran through {@link #getIdFromKey(RocksDB, ColumnFamilyHandle, ColumnFamilyHandle, String)}
     *
     * @param rocksDB
     * @param idToKeyColHandle
     * @param id
     * @return
     * @throws RocksDBException
     */
    public static String getKeyFromId(RocksDB rocksDB, ColumnFamilyHandle idToKeyColHandle, String id) throws RocksDBException {
        return new String(rocksDB.get(idToKeyColHandle, id.getBytes()));
    }
}
