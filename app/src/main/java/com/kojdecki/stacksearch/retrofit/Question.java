package com.kojdecki.stacksearch.retrofit;

/**
 * Created by calot on 2/15/16.
 */
public class Question {
    int answer_count;
    ShallowUser owner;
    String title;

    public String toString() {
        return new StringBuilder().append("AnswerCount: ")
                .append(answer_count)
                .append(" Title: ")
                .append(title)
                .append(" Owner: ")
                .append(owner.toString()).toString();
    }

    public String getTitle() {
        return title;
    }

    public ShallowUser getOwner() {
        return owner;
    }

    public int getAnswer_count() {
        return answer_count;
    }
}
