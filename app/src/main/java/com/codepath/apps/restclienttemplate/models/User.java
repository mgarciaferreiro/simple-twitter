package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class User {

    public String name;
    public long uid;
    public String screenName;
    public String profileImageUrl;
    public String backgroundImageUrl;
    public Integer followersCount;
    public Integer friendsCount;
    public String description;

    public static User fromJSON(JSONObject json) throws JSONException {
        User user = new User();
        user.name = json.getString("name");
        user.uid = json.getLong("id");
        user.screenName = json.getString("screen_name");
        user.profileImageUrl = json.getString("profile_image_url_https");
        if (json.has("profile_banner_url")) {
            user.backgroundImageUrl = json.getString("profile_banner_url");
        }
        user.followersCount = json.getInt("followers_count");
        user.friendsCount = json.getInt("friends_count");
        user.description = json.getString("description");
        return user;
    }
}
