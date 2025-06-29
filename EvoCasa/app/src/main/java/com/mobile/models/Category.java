package com.mobile.models;

import java.io.Serializable;

public class Category implements Serializable {
    private String id;
    private String name;
    private String parentCategoryId; // Added to match the query
    private int imageResId;

    public Category() {}

    public Category(int imageResId, String name) {
        this.imageResId = imageResId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}