package com.planetpeopleplatform.freegan.model;


import java.util.HashMap;


public class Post {

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

}