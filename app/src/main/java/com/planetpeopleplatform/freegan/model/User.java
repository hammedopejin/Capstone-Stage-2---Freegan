package com.planetpeopleplatform.freegan.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.planetpeopleplatform.freegan.utils.Constants;

import static com.planetpeopleplatform.freegan.utils.Constants.kBLOCKEDUSERSLIST;
import static com.planetpeopleplatform.freegan.utils.Constants.kEMAIL;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERIMAGEURL;

public class User implements Parcelable{

    private static final String PLACE_HOLDER = "placeHolder";

    private String objectId;
    private String pushId;
    private String createdAt;
    private String updatedAt;
    private String email;
    private String userName;
    private String userImgUrl = "";
    private String loginMethod;
    private String status = "";
    private Double latitude;
    private Double longitude;
    private ArrayList<String> blockedUsersList = new ArrayList<>();


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
        this.blockedUsersList.add(PLACE_HOLDER);
    }

    public User (HashMap<String, Object> dictionary){

        objectId = (String) dictionary.get(Constants.kOBJECTID);
        pushId = (String) dictionary.get(Constants.kPUSHID);
        createdAt = (String) dictionary.get(Constants.kCREATEDAT);
        updatedAt = (String) dictionary.get(Constants.kUPDATEDAT);
        email = (String) dictionary.get(kEMAIL);
        userName = (String) dictionary.get(Constants.kUSERNAME);
        userImgUrl = (String) dictionary.get(kUSERIMAGEURL);
        loginMethod = (String) dictionary.get(Constants.kLOGINMETHOD);
        latitude = (Double) dictionary.get(Constants.kLATITUDE);
        longitude = (Double) dictionary.get(Constants.kLONGITUDE);
        blockedUsersList = (ArrayList<String>) dictionary.get(kBLOCKEDUSERSLIST);
    }

    private static SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
    private static Date dataobj= new Date();


    protected User(Parcel in) {
        objectId = in.readString();
        pushId = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
        email = in.readString();
        userName = in.readString();
        userImgUrl = in.readString();
        loginMethod = in.readString();
        status = in.readString();
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        blockedUsersList = in.createStringArrayList();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public ArrayList<String> getBlockedUsersList() {
        return blockedUsersList;
    }

    public void setBlockedUsersList(ArrayList<String> blockedUsersList) {
        this.blockedUsersList = blockedUsersList;
    }

    public void addBlockedUser(String blockedUserId) {
        if(this.blockedUsersList != null) {
            if (!(this.blockedUsersList.contains(blockedUserId))) {
                this.blockedUsersList.add(blockedUserId);
            }
        }
    }

    public void removeBlockedUser(String blockedUserId){
        if(this.blockedUsersList != null) {
            if (this.blockedUsersList.contains(blockedUserId)) {
                this.blockedUsersList.remove(blockedUserId);
            }
        }
    }

    //Set User status
    public void put(String status, Boolean online){
        if (online){
            this.status = status;
        }else{
            this.status = "offline";
        }
    }

    public boolean getBoolean(String status) {
        return status.equals(this.status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(objectId);
        parcel.writeString(pushId);
        parcel.writeString(createdAt);
        parcel.writeString(updatedAt);
        parcel.writeString(email);
        parcel.writeString(userName);
        parcel.writeString(userImgUrl);
        parcel.writeString(loginMethod);
        parcel.writeString(status);
        if (latitude == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(latitude);
        }
        if (longitude == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(longitude);
        }
        parcel.writeStringList(blockedUsersList);
    }
}