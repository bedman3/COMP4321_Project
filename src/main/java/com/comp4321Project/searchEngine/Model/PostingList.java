package com.comp4321Project.searchEngine.Model;

import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;

import java.util.LinkedList;

public class PostingList {
    private LinkedList<PostingNode> postingList;

    public static PostingList fromBytesArray(byte[] byteArray) {
        if (byteArray == null) {
            return new PostingList();
        } else {
            return (PostingList) CustomFSTSerialization.getInstance().asObject(byteArray);
        }
    }

    public byte[] toBytesArray() {
        return CustomFSTSerialization.getInstance().asByteArray(postingList);
    }

    public void add(String urlId, int location) {
    }
}
