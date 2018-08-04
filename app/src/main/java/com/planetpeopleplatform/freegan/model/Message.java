package com.planetpeopleplatform.freegan.model;

public class Message {

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
}
