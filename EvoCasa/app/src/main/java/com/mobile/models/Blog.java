package com.mobile.models;

public class Blog {
    private final String title;
    private final String date;
    private final int imageResId;

    public Blog(String title, String date, int imageResId) {
        this.title = title;
        this.date = date;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public int getImageResId() {
        return imageResId;
    }
}
