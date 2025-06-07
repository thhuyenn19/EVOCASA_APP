package com.mobile.models;

public class Collection {
    private int imageResId;
    private String name;

    public Collection(int imageResId, String name) {
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
