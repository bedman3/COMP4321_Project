package com.comp4321Project.searchEngine.Model;

import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;

import java.io.Serializable;
import java.util.*;

public class PostingList implements Serializable {
    private final ArrayList<PostingNode> postingList;

    public PostingList() {
        this.postingList = new ArrayList<>();
    }

    public PostingList(ArrayList<PostingNode> arrayList) {
        this.postingList = arrayList;
    }

    public static PostingList fromBytesArray(byte[] byteArray) {
        if (byteArray == null) {
            return new PostingList();
        } else {
            return (PostingList) CustomFSTSerialization.getInstance().asObject(byteArray);
        }
    }

    public byte[] toBytesArray() {
        return CustomFSTSerialization.getInstance().asByteArray(this);
    }

    public PostingNode binarySearch(String urlId, String wordId) {
//        PostingNode node = new PostingNode(wordId, urlId);
//        int index = Arrays.binarySearch(postingList.toArray(), node);
        for (int index = 0; index < postingList.size(); index++) {
            PostingNode node = null;
            try {
                node = this.postingList.get(index);
            } catch (Exception e) {
                this.postingList.get(index);
            }

            if (node.getUrlId().equals(urlId)) {
                return node;
            }
        }
        return new PostingNode(wordId, urlId);
    }

    public void add(String wordId, String urlId, int location, Set<PostingNode> lazyNodeSet, boolean lazy) {
        // overwrite the posting node every new indexing so as to avoid duplicating data
        PostingNode node = this.binarySearch(urlId, wordId);

        node.addLocation(location, lazy);
        lazyNodeSet.add(node);
        postingList.add(node);
    }

    // merge the new posting list into our existing posting list sorted by url id
    public void merge(PostingList newPostingList) {
        this.postingList.addAll(newPostingList.postingList);
//        Collections.sort(this.postingList);
    }

    @Override
    public String toString() {
        return Arrays.toString(postingList.toArray());
    }
}
