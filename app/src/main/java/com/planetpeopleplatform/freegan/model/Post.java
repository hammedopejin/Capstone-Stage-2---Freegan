package com.planetpeopleplatform.freegan.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

import static com.planetpeopleplatform.freegan.utils.Constants.kDESCRIPTION;
import static com.planetpeopleplatform.freegan.utils.Constants.kIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTUSEROBJECTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPROFILEIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERNAME;


public class Post implements Parcelable{

    private String description;
    private ArrayList<String> imageUrl = new ArrayList<>();
    private String userName;
    private String postUserObjectId;
    private String profileImgUrl;
    private String postDate;
    private String postId;

    public Post() {
    }

    public Post(HashMap<String, Object> postData) {
        this.postUserObjectId = (String) postData.get(kPOSTUSEROBJECTID);
        this.description = (String) postData.get(kDESCRIPTION);
        this.imageUrl = (ArrayList<String>) postData.get(kIMAGEURL);
        this.profileImgUrl = (String) postData.get(kPROFILEIMAGEURL);
        this.userName = (String) postData.get(kUSERNAME);
        this.postDate = (String) postData.get(kPOSTDATE);
        this.postId = (String) postData.get(kPOSTID);
    }

    public Post (String postId, String postUserObjectId,
                 String description, ArrayList<String> imageUrl, String profileImgUrl, String userName, String postDate) {
        this.postId = postId;
        this.postUserObjectId = postUserObjectId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.profileImgUrl = profileImgUrl;
        this.userName = userName;
        this.postDate = postDate;
    }


    protected Post(Parcel in) {
        description = in.readString();
        imageUrl = in.createStringArrayList();
        userName = in.readString();
        postUserObjectId = in.readString();
        profileImgUrl = in.readString();
        postDate = in.readString();
        postId = in.readString();
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

    public String getPostId () { return this.postId; }
    public void setPostId(String postId) {
        this.postId = postId;
    }

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

    public ArrayList<String> getImageUrl () {
        return this.imageUrl;
    }
    public void setImageUrl (ArrayList<String> imageUrl){
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
        parcel.writeStringList(imageUrl);
        parcel.writeString(userName);
        parcel.writeString(postUserObjectId);
        parcel.writeString(profileImgUrl);
        parcel.writeString(postDate);
        parcel.writeString(postId);
    }
}