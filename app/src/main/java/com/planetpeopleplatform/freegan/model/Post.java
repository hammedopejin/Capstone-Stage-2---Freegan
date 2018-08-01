package com.planetpeopleplatform.freegan.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;


public class Post implements Parcelable {

    private String description;
    private String imageUrl;
    private String userName;
    private String postUserObjectId;
    private String profileImgUrl;
    private String postDate;

    public Post() {
    }

    public Post(HashMap<String, Object> postData) {
        this.postUserObjectId = (String) postData.get("postUserObjectId");
        this.description = (String) postData.get("description");
        this.imageUrl = (String) postData.get("imageUrl");
        this.profileImgUrl = (String) postData.get("profileImgUrl");
        this.userName = (String) postData.get("userName");
        this.postDate = (String) postData.get("postDate");
    }

    public Post (String postUserObjectId, String description, String imageUrl, String profileImgUrl, String userName, String postDate) {
        this.postUserObjectId = postUserObjectId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.profileImgUrl = profileImgUrl;
        this.userName = userName;
        this.postDate = postDate;
    }

    protected Post(Parcel in) {
        description = in.readString();
        imageUrl = in.readString();
        userName = in.readString();
        postUserObjectId = in.readString();
        profileImgUrl = in.readString();
        postDate = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getPostUserObjectId () {
        return this.postUserObjectId;
    }
    public void setPostUserObjectId (String postUserObjectId){
        this.postUserObjectId = postUserObjectId;
    }

    public String getDescription () {
        return this.description;
    }
    public void setDescription (String description){
        this.description = description;
    }

    public String getImageUrl () {
        return this.imageUrl;
    }
    public void setImageUrl (String imageUrl){
        this.imageUrl = imageUrl;
    }

    public String getProfileImgUrl () {
        return this.profileImgUrl;
    }
    public void setProfileImgUrl (String profileImgUrl){
        this.profileImgUrl = profileImgUrl;
    }

    public String getUserName () {
        return userName;
    }
    public void setUserName (String userName){
        this.userName = userName;
    }

    public String getPostDate() {
        return this.postDate;
    }
    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(description);
        parcel.writeString(imageUrl);
        parcel.writeString(userName);
        parcel.writeString(postUserObjectId);
        parcel.writeString(profileImgUrl);
        parcel.writeString(postDate);
    }
}