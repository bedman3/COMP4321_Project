package com.comp4321Project.searchEngine.Util;

import com.comp4321Project.searchEngine.Model.InvertedFile;
import com.comp4321Project.searchEngine.Model.PostingList;
import org.nustaq.serialization.FSTConfiguration;

import java.util.HashMap;
import java.util.HashSet;

public class CustomFSTSerialization {
    private final static FSTConfiguration singletonConfig = FSTConfiguration.createDefaultConfiguration();
    static {
        singletonConfig.registerClass(
                HashMap.class,
                HashSet.class,
                PostingList.class
        );
    }

    public static FSTConfiguration getInstance() {
        return singletonConfig;
    }
}
