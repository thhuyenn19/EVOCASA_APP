package com.mobile.models;

import java.util.Date;

public class ChatMessage {
    private String message;
    private String senderId;
    private Date timestamp;
    private boolean isRead;

    // Constructor mặc định (yêu cầu cho Firestore)
    public ChatMessage() {
    }

    // Constructor với tham số
    public ChatMessage(String message, String senderId, Date timestamp, boolean isRead) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    // Getter và Setter
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}