package com.comp4321Project.searchEngine.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PostingNode implements Serializable, Comparable<PostingNode> {
    private final String wordId;
    private final String urlId;
    private final Integer urlIdInteger;
    private final ArrayList<Integer> locationList;

    public PostingNode(String wordId, String urlId, Integer urlIdInteger, ArrayList<Integer> locationList) {
        this.urlId = urlId;
        this.wordId = wordId;
        this.urlIdInteger = Integer.parseInt(urlId);
        this.locationList = locationList;
    }

    public PostingNode(String wordId, String urlId, ArrayList<Integer> locationList) {
        this.urlId = urlId;
        this.wordId = wordId;
        this.urlIdInteger = Integer.parseInt(urlId);
        this.locationList = locationList;
    }

    public PostingNode(String wordId, String urlId) {
        this.urlId = urlId;
        this.wordId = wordId;
        this.urlIdInteger = Integer.parseInt(urlId);
        locationList = new ArrayList<>();
    }

    public Integer getUrlIdInteger() {
        return urlIdInteger;
    }

    public ArrayList<Integer> getLocationList() {
        return locationList;
    }

    public void addLocation(Integer location, boolean lazy) {
        this.locationList.add(location);

        if (!lazy) {
            Collections.sort(this.locationList);
        }
    }

    public void sort() {
        Collections.sort(this.locationList);
    }

    @Override
    public int hashCode() {
        return (wordId + "_" + urlId).hashCode();
    }

    @Override
    public String toString() {
        return String.format("(urlId %s, locations: %s)", urlId, Arrays.toString(locationList.toArray()));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PostingNode) {
            PostingNode node = (PostingNode) o;
            return this.urlId.equals(node.urlId) && this.wordId.equals(node.wordId);
        }
        return false;
    }

    public String getWordId() {
        return wordId;
    }

    public String getUrlId() {
        return urlId;
    }

    @Override
    public int compareTo(PostingNode postingNode) {
        return this.urlIdInteger.compareTo(postingNode.urlIdInteger);
    }
}
