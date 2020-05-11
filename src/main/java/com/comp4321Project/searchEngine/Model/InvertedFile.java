package com.comp4321Project.searchEngine.Model;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.*;
import java.util.stream.Collectors;

public class InvertedFile {
    private final RocksDBDao rocksDBDao;
    private final ColumnFamilyHandle colHandle;
    private final HashMap<String, PostingList> hashMap;
    private final HashSet<PostingNode> lazySortNodeSet;

    public InvertedFile(RocksDBDao rocksDBDao, ColumnFamilyHandle colHandle) {
        this.rocksDBDao = rocksDBDao;
        this.colHandle = colHandle;
        this.hashMap = new HashMap<String, PostingList>();
        this.lazySortNodeSet = new HashSet<>();
    }

    public void add(String wordId, String urlId, int location, boolean lazy) throws RocksDBException {
        // check if the wordId exists in the hashMap
        PostingList postingList = this.hashMap.get(wordId);
        if (postingList == null) {
            // if posting list is not initialized in the in-memory hashmap, import posting list from rocksdb
            postingList = PostingList.fromBytesArray(this.rocksDBDao.getRocksDB().get(colHandle, wordId.getBytes()));

        }
        postingList.add(wordId, urlId, location, lazySortNodeSet, lazy);
        this.hashMap.put(wordId, postingList);
    }

    public HashSet<String> loadInvertedFileWithWordId(List<byte[]> wordIdList) throws RocksDBException {
        RocksDB rocksDB = rocksDBDao.getRocksDB();
        HashSet<String> hashSet = new HashSet<>();

        for (byte[] wordIdByte : wordIdList) {
            PostingList postingList = PostingList.fromBytesArray(rocksDB.get(this.colHandle, wordIdByte));
            hashSet.addAll(postingList.getAllUrlIdFromPostingList());
            this.hashMap.put(new String(wordIdByte), postingList);
        }

        return hashSet;
    }

    public PostingList getPostingListWithWordId(String wordId) throws RocksDBException {
        return PostingList.fromBytesArray(rocksDBDao.getRocksDB().get(colHandle, wordId.getBytes()));
    }

    /**
     * this function will do:
     * 1) scan the posting list required to update
     * 2) import posting list from rocksdb
     * 3) if there does not exist posting list for keyword, skip merging and use the posting list from hash map
     * 4) if there exists posting list for keyword, import it to memory, merge it with the existing list
     * by default will do the sorting for the location list in posting list
     */
    public void mergeExistingWithRocksDB() throws RocksDBException {
        for (PostingNode node : lazySortNodeSet) {
            // sort the order of the locations in doc list
            node.sort();
        }
        // clear the set after sorting
        lazySortNodeSet.clear();

        for (Map.Entry<String, PostingList> entry : hashMap.entrySet()) {
            PostingList postingList;
            byte[] postingListByteFromRocksDB = rocksDBDao.getRocksDB().get(colHandle, entry.getKey().getBytes());
            if (postingListByteFromRocksDB == null) {
                // if posting list does not exist in rocksdb
                // do nothing here, no need to update the in memory posting list
            } else {
                // read the posting list
                postingList = PostingList.fromBytesArray(postingListByteFromRocksDB);

                // merge the in memory posting list into the existing posting list
                postingList.merge(entry.getValue());
                hashMap.put(entry.getKey(), postingList);
            }
        }
    }

    public void flushToRocksDB() throws RocksDBException {
        RocksDB rocksDB = this.rocksDBDao.getRocksDB();
        for (Map.Entry<String, PostingList> entry : this.hashMap.entrySet()) {
            rocksDB.put(this.colHandle, entry.getKey().getBytes(), entry.getValue().toBytesArray());
            // record the total number of docs this word appear in
            if (this.colHandle == rocksDBDao.getInvertedFileForBodyWordIdToPostingListRocksDBCol()) {
                rocksDB.put(rocksDBDao.getWordIdToDocumentFrequencyRocksDBCol(), entry.getKey().getBytes(), entry.getValue().size().toString().getBytes());
            }
        }
        this.hashMap.clear();
    }

    public HashSet<String> loadInvertedFileWithPhrases(ArrayList<ArrayList<String>> phrasesListInWordId) throws RocksDBException {
        RocksDB rocksDB = rocksDBDao.getRocksDB();
        HashSet<String> returnSet = null;
        HashMap<String, PostingList> cacheMap = new HashMap<>();

        // Each phrase have an AND relationship, check each phrase, if no result match, move on and search for another phrase
        for (List<String> phraseWithWordId : phrasesListInWordId) {
            HashSet<ImmutablePair<Integer, String>> phraseDocSet = null;

            if (phraseWithWordId.size() > 1) {
                // get the posting list for each 2-gram, and check if there is any document contains them
                for (int index = 1; index < phraseWithWordId.size(); index++) {
                    // get the posting list of the two phrases and start finding relevant document
                    PostingList postingList1 = cacheMap.get(phraseWithWordId.get(index - 1));
                    PostingList postingList2 = cacheMap.get(phraseWithWordId.get(index));
                    if (postingList1 == null) {
                        postingList1 = PostingList.fromBytesArray(rocksDB.get(this.colHandle, phraseWithWordId.get(index - 1).getBytes()));
                        cacheMap.put(phraseWithWordId.get(index - 1), postingList1);
                    }
                    if (postingList2 == null) {
                        postingList2 = PostingList.fromBytesArray(rocksDB.get(this.colHandle, phraseWithWordId.get(index).getBytes()));
                        cacheMap.put(phraseWithWordId.get(index), postingList2);
                    }

                    HashSet<ImmutablePair<Integer, String>> commonDoc = PostingList.findCommonSetBetweenPhrase(postingList1, postingList2, index, phraseDocSet);
                    if (phraseDocSet == null) phraseDocSet = commonDoc;
                    else phraseDocSet.retainAll(commonDoc); // get intersection

                    if (phraseDocSet.size() == 0) break; // do not have to proceed getting common document
                }

                // put the results back to the return set
                if (phraseDocSet.size() == 0) {
                    break;
                } else {
                    HashSet<String> parsedPhraseDocSet = phraseDocSet.stream().map(ImmutablePair::getRight).distinct().collect(Collectors.toCollection(HashSet::new));
                    // add the results back to the large return set, get the intersection
                    if (returnSet == null) returnSet = parsedPhraseDocSet;
                    else returnSet.retainAll(parsedPhraseDocSet);
                }
            } else if (phraseWithWordId.size() == 1) {
                PostingList getList = cacheMap.get(phraseWithWordId.get(0));
                if (getList == null) {
                    getList = getPostingListWithWordId(phraseWithWordId.get(0));
                    cacheMap.put(phraseWithWordId.get(0), getList);
                }
                HashSet<String> result = getList.getAllUrlIdFromPostingList();
                if (returnSet == null) returnSet = result;
                else returnSet.retainAll(result);
            } else continue;

            if (returnSet != null && returnSet.size() == 0) return returnSet; // no match available, return empty set
        }

        return returnSet == null ? new HashSet<>() : returnSet;
    }
}
