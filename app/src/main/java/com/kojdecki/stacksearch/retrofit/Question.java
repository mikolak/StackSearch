package com.kojdecki.stacksearch.retrofit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by calot on 2/15/16.
 */
public class Question implements Parcelable {
    private int answer_count;
    private ShallowUser owner;
    private String title;
    private String link;

    public Question(int answer_count, ShallowUser owner, String title, String link) {
        this.answer_count = answer_count;
        this.owner = owner;
        this.title = title;
        this.link = link;
    }
    public String toString() {
        return new StringBuilder().append("AnswerCount: ")
                .append(answer_count)
                .append(" Title: ")
                .append(title)
                .append(" Owner: ")
                .append(owner.toString())
                .append(" Link: ")
                .append(link).toString();
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

    public String getLink() {
        return link;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(answer_count);
        dest.writeParcelable(owner, flags);
        dest.writeString(title);
        dest.writeString(link);
    }

    public static final Parcelable.Creator<Question> CREATOR =
            new Parcelable.Creator<Question>() {
                public Question createFromParcel (Parcel in) {
                    return new Question(in);
                }

                @Override
                public Question[] newArray(int size) {
                    return new Question[size];
                }
            };

    private Question(Parcel in) {
        answer_count = in.readInt();
        owner = (ShallowUser) in.readParcelable(null);
        title = in.readString();
        link = in.readString();
    }

}
