package com.comp4321Project.searchEngine.Util;

import org.nustaq.serialization.FSTConfiguration;

import java.util.HashMap;
import java.util.HashSet;

public class CustomFSTSerialization {
    private final static FSTConfiguration singletonConfig = FSTConfiguration.createDefaultConfiguration();
    static {
        singletonConfig.registerClass(HashMap.class, HashSet.class);
    }

    public static FSTConfiguration getInstance() {
        return singletonConfig;
    }
}
