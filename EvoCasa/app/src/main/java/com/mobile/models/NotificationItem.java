package com.mobile.models;

public class NotificationItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NOTIFICATION = 1;

    private int type;

    // Header
    private String headerTitle;

    // Notification
    private int iconResId;
    private String title;
    private String message;
    private String time;
    private boolean isRead;

    // Constructor cho Header
    public NotificationItem(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    // Constructor cho Notification
    public NotificationItem(int iconResId, String title, String message, String time, boolean isRead) {
        this.type = TYPE_NOTIFICATION;
        this.iconResId = iconResId;
        this.title = title;
        this.message = message;
        this.time = time;
        this.isRead = isRead;
    }

    public int getType() { return type; }

    public String getHeaderTitle() { return headerTitle; }

    public int getIconResId() { return iconResId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }
}
