package com.mobile.models;

public class Notification {
    private int iconResId;
    private String title;
    private String message;
    private String time;

    public Notification(int iconResId, String title, String message, String time) {
        this.iconResId = iconResId;
        this.title = title;
        this.message = message;
        this.time = time;
    }

    public int getIconResId() { return iconResId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
}
