package com.comp4321Project.searchEngine.Util;

import com.comp4321Project.searchEngine.Model.PostingList;
import com.comp4321Project.searchEngine.Model.PostingNode;
import com.comp4321Project.searchEngine.View.QuerySearchResponseView;
import com.comp4321Project.searchEngine.View.SiteMetaData;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
                ArrayList.class,
                ImmutablePair.class,
                SiteMetaData.class,
                QuerySearchResponseView.class
        );
    }

    public static FSTConfiguration getInstance() {
        return singletonConfig;
    }
}
