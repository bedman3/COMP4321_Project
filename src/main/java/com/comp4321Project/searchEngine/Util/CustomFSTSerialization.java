package com.comp4321Project.searchEngine.Util;

import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Model.PostingList;
import com.comp4321Project.searchEngine.Model.PostingNode;
import org.nustaq.serialization.FSTConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CustomFSTSerialization {
    private final static FSTConfiguration singletonConfig = FSTConfiguration.createDefaultConfiguration();
    static {
        singletonConfig.registerClass(
                HashMap.class,
                HashSet.class,
                PostingList.class,
                PostingNode.class,
                ArrayList.class
        );
    }

    public static FSTConfiguration getInstance() {
        return singletonConfig;
    }
}
