package com.kojdecki.stacksearch.retrofit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by calot on 2/15/16.
 */
public class ShallowUser implements Parcelable {
    private String display_name;
    private String profile_image;

    public ShallowUser(String display_name, String profile_image) {
        this.display_name = display_name;
        this.profile_image = profile_image;
    }

    public ShallowUser(Parcel source) {
        display_name = source.readString();
        profile_image = source.readString();
    }

    public String toString() {
        return new StringBuilder().append("Display name: ")
                .append(display_name)
                .append(" Profile image URL: ")
                .append(profile_image).toString();
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getProfile_image() {
        return profile_image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(display_name);
        dest.writeString(profile_image);
    }

    public static final Parcelable.Creator<ShallowUser> CREATOR
            = new Parcelable.Creator<ShallowUser>() {

        @Override
        public ShallowUser createFromParcel(Parcel source) {
            return new ShallowUser(source);
        }

        @Override
        public ShallowUser[] newArray(int size) {
            return new ShallowUser[size];
        }
    };
}
