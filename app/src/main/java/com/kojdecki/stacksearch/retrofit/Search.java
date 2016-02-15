package com.kojdecki.stacksearch.retrofit;

import java.util.List;

/**
 * Created by calot on 2/15/16.
 */
public class Search {
    private List<Question> items;

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Question item : items) {
            stringBuilder.append("Item: ")
                    .append(item.toString())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    public List<Question> getItems() {
        return items;
    }
}
