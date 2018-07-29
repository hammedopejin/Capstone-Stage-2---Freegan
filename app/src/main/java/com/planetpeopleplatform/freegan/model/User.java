package com.planetpeopleplatform.freegan.model;

import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.planetpeopleplatform.freegan.utils.Constants;
import static com.planetpeopleplatform.freegan.utils.Constants.kEMAIL;

public class User {

    private String objectId;
    private String pushId;
    private String createdAt;
    private String updatedAt;
    private String email;
    private String userName;
    private String userImgUrl = "";
    private String loginMethod;
    private String status = "";


    public User (String _objectId, String _pushId, String _createdAt, String _updatedAt, String _email,
                 String _username, String _userimgurl, String _loginMethod) {

        this.objectId = _objectId;
        this.pushId = _pushId;

        this.createdAt = _createdAt;
        this.updatedAt = _updatedAt;

        this.email = _email;
        this.userName = _username;
        this.userImgUrl = _userimgurl;

        this.loginMethod = _loginMethod;

    }

    public User (HashMap<String, Object> dictionary){

        objectId = (String) dictionary.get(Constants.kOBJECTID);
        pushId = (String) dictionary.get(Constants.kPUSHID);
        createdAt = (String) dictionary.get(Constants.kCREATEDAT);
        updatedAt = (String) dictionary.get(Constants.kUPDATEDAT);
        email = (String) dictionary.get(kEMAIL);
        userName = (String) dictionary.get(Constants.kUSERNAME);
        userImgUrl = (String) dictionary.get(Constants.kAVATAR);
        loginMethod = (String) dictionary.get(Constants.kLOGINMETHOD);

    }

    static SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
    static Date dataobj= new Date();

    public static void registerUserWith(String email, String fireUserUid, String userName)
        {

            User fuser = new User(fireUserUid, "", sfd.format(dataobj),
                    sfd.format(dataobj), email, userName, "", kEMAIL);

            fuser.saveUserInBackground(fuser);

        }

    //Save user funcs
    private void saveUserInBackground(User fuser) {

        DatabaseReference ref = Constants.firebase.child(Constants.kUSER).child(fuser.objectId);
        ref.setValue(fuser);

    }



    public String getObjectId () {
        return objectId;
    }
    public void setObjectId (String objectId){
        this.objectId = objectId;
    }

    public String getPushId () {
        return pushId;
    }
    public void setPushId (String pushId){
        this.pushId = pushId;
    }

    public String getCreatedAt () {
        return createdAt;
    }
    public void setCreatedAt (String createdAt){
        this.createdAt = createdAt;
    }

    public String getUpdatedAt () {
        return updatedAt;
    }
    public void setUpdatedAt (String updatedAt){
        this.updatedAt = updatedAt;
    }

    public String getEmail () {
        return email;
    }
    public void setEmail (String email){
        this.email = email;
    }

    public String getUserName () {
        return userName;
    }
    public void setUserName (String userName){
        this.userName = userName;
    }

    public String getLoginMethod() {
        return loginMethod;
    }
    public void setLoginMethod(String loginMethod) {
        this.loginMethod = loginMethod;
    }

    public String getUserImgUrl() {
        return userImgUrl;
    }
    public void setUserImgUrl(String userImgUrl) { this.userImgUrl = userImgUrl; }

    public String getStatus() {
        return status;
    }
    public  void setStatus(String status) {
        this.status = status;
    }

    //Set User status
    void put(String status, Boolean online){
        if (online){
            this.status = status;
        }else{
            this.status = "offline";
        }
    }

    Boolean getBoolean(String status) {
        return status.equals(this.status);
    }


}
