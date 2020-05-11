package com.comp4321Project.searchEngine.Model;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class PostingNode implements Serializable, Comparable<PostingNode> {
    private final String wordId;
    private final String urlId;
    private final Integer urlIdInteger;
    private ArrayList<Integer> locationList;

    public PostingNode(String wordId, String urlId, Integer urlIdInteger, ArrayList<Integer> locationList) {
        this.urlId = urlId;
        this.wordId = wordId;
        this.urlIdInteger = urlIdInteger;
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

    public void merge(PostingNode node) {
        this.locationList.addAll(node.locationList);
        this.locationList = this.locationList
                .stream()
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public int hashCode() {
        return (wordId + "_" + urlId).hashCode();
    }

    @Override
    public String toString() {
        return String.format("(urlId: %s, locations: %s)", urlId, Arrays.toString(locationList.toArray()));
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

    public HashSet<ImmutablePair<Integer, String>> isNextWordAPhrase(PostingNode nextNode, int index, HashSet<ImmutablePair<Integer, String>> phraseDocSet) {
        if (!this.urlIdInteger.equals(nextNode.urlIdInteger)) return null;

        // thisNode and nextNode have the same urlId but different wordId
        // this node is ahead of the nextNode, so this node index + 1 = next node index means a phrase
        // minus 1 for each of the element in next node, if there is a match with next node, return true

        if (phraseDocSet == null) {
            HashSet<ImmutablePair<Integer, String>> result = new HashSet<>();
            HashSet<Integer> thisSet = new HashSet<>(this.locationList);
            HashSet<Integer> nextSet = nextNode.locationList
                    .stream()
                    .map(integer -> integer - index)
                    .collect(Collectors.toCollection(HashSet::new));

            thisSet.retainAll(nextSet); // find intersection
            thisSet.forEach(location -> result.add(new ImmutablePair<>(location, this.urlId)));
            return result;
        } else {
            HashSet<ImmutablePair<Integer, String>> thisSet = new HashSet<>(phraseDocSet);
            HashSet<ImmutablePair<Integer, String>> nextSet = new HashSet<>();
            nextNode.locationList
                    .stream()
                    .map(integer -> integer - index)
                    .forEach(location -> nextSet.add(new ImmutablePair<>(location, this.urlId)));
            thisSet.retainAll(nextSet);
            return thisSet;
        }
    }
}
