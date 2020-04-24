package com.comp4321Project.searchEngine.Model;

import com.comp4321Project.searchEngine.Util.CustomFSTSerialization;

public class PostingList {
    public static InvertedFile fromBytesArray(byte[] byteArray) {
        return (InvertedFile) CustomFSTSerialization.getInstance().asObject(byteArray);
    }

    public byte[] toBytesArray() {
        return CustomFSTSerialization.getInstance().asByteArray()
    }
}
