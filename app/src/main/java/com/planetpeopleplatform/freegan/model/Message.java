package com.planetpeopleplatform.freegan.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {

    public final static String STATUS_READ = "Read";
    public final static String STATUS_DELIVERED = "Delivered";
    public final static String STATUS_FAILED = "Failed";

    //String sender = null
    private String message = "";
    private String senderId = "";
    private String messageId = "";
    private String senderName = "";
    private String date = "";
    private String status = "";
    private String type = "";




    public Message(String message, String date, String messageId, String senderId,
                   String senderName, String status, String type){
        this.message = message;
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.date = date;
        this.status = status;
        this.type = type;
    }


    protected Message(Parcel in) {
        message = in.readString();
        senderId = in.readString();
        messageId = in.readString();
        senderName = in.readString();
        date = in.readString();
        status = in.readString();
        type = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(message);
        parcel.writeString(senderId);
        parcel.writeString(messageId);
        parcel.writeString(senderName);
        parcel.writeString(date);
        parcel.writeString(status);
        parcel.writeString(type);
    }
}
