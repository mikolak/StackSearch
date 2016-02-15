package com.kojdecki.stacksearch.retrofit;

/**
 * Created by calot on 2/15/16.
 */
public class ShallowUser {
    public String display_name;
    public String profile_image;

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
}
