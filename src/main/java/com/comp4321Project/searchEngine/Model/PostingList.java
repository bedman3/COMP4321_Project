package com.comp4321Project.searchEngine.Model;

import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;

import java.io.Serializable;
import java.util.*;

public class PostingList implements Serializable {
    private List<PostingNode> postingList;

    public PostingList() {
        this.postingList = new ArrayList<>();
    }

    public PostingList(ArrayList<PostingNode> arrayList) {
        this.postingList = arrayList;
    }

    public static PostingList fromBytesArray(byte[] byteArray) {
        if (byteArray == null) return new PostingList();
        else return (PostingList) CustomFSTSerialization.getInstance().asObject(byteArray);
    }

    public byte[] toBytesArray() {
        return CustomFSTSerialization.getInstance().asByteArray(this);
    }

    public PostingNode search(String urlId) {
        PostingNode node;

        for (int index = 0; index < postingList.size(); index++) {
            node = this.postingList.get(index);

            if (node.getUrlId().equals(urlId)) {
                return node;
            }
        }
        return null;
    }

    public void add(String wordId, String urlId, int location, Set<PostingNode> lazyNodeSet, boolean lazy) {
        // overwrite the posting node every new indexing so as to avoid duplicating data
        PostingNode node = this.search(urlId);
        boolean addNodeToPostingList = false;
        if (node == null) {
            addNodeToPostingList = true;
            node = new PostingNode(wordId, urlId);
        }

        node.addLocation(location, lazy);
        lazyNodeSet.add(node);
        if (addNodeToPostingList) {
            postingList.add(node);
        }
    }

    // merge the new posting list into our existing posting list sorted by url id
    public void merge(PostingList newPostingList) {
        this.postingList.sort(Comparator.comparing(PostingNode::getUrlIdInteger));
        newPostingList.postingList.sort(Comparator.comparing(PostingNode::getUrlIdInteger));

        int thisListLength = this.postingList.size(), newListLength = newPostingList.postingList.size(), thisPtr = 0, newPtr = 0;

        while (thisPtr < thisListLength || newPtr < newListLength) {
            if (newPtr >= newListLength) {
                // finish iterating the new list, nothing more to merge into this list
                break;
            } else if (thisPtr >= thisListLength) {
                // reached the end of this ptr, keep adding node from new list
                this.postingList.add(newPostingList.postingList.get(newPtr));
                newPtr++;
                continue;
            }

            PostingNode thisNode = this.postingList.get(thisPtr), newNode = newPostingList.postingList.get(newPtr);

            int compare = thisNode.getUrlIdInteger().compareTo(newNode.getUrlIdInteger());
            if (compare == 0) {
                thisNode.merge(newNode);
                newPtr++;
                thisPtr++;
            } else if (compare > 0) {
                // new node not exist in this list, add new node to this list
                this.postingList.add(newNode);
                newPtr++;
            } else { // compare < 0, thisNode int value < nextNode int value
                thisPtr++;
            }

        }

        this.postingList.sort(Comparator.comparing(PostingNode::getUrlIdInteger));
    }

    public Integer size() {
        return this.postingList.size();
    }

    @Override
    public String toString() {
        return Arrays.toString(postingList.toArray());
    }

    public HashSet<String> getAllUrlIdFromPostingList() {
        HashSet<String> hashSet = new HashSet<>();
        for (PostingNode node : postingList) {
            hashSet.add(node.getUrlId());
        }
        return hashSet;
    }
}
