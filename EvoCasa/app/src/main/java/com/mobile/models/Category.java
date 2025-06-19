package com.mobile.models;

public class Category {
    private String name;
    private int imageResId;

    public Category() {}

    public Category(int imageResId, String name) {
        this.imageResId = imageResId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}
