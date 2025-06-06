package com.mobile.models;

public class Category {
    private int imageResId;
    private String name;

    public Category(int imageResId, String name) {
        this.imageResId = imageResId;
        this.name = name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getName() {
        return name;
    }
}
