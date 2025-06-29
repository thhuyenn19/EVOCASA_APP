package com.thanhhuyen.evocasaadmin;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Product {
    @Exclude
    private String id;
    private String name;
    private String description;
    private String categoryId;
    private List<String> images;
    private double price;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    public Product() {
        // Required empty constructor for Firestore
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getImages() {
        return images != null ? images : new ArrayList<>();
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    @PropertyName("image")
    public void setImageFromString(String imageJson) {
        if (imageJson == null || imageJson.isEmpty()) {
            this.images = new ArrayList<>();
            return;
        }
        
        try {
            if (imageJson.startsWith("[")) {
                // If it's a JSON array string
                this.images = new Gson().fromJson(imageJson, new TypeToken<List<String>>(){}.getType());
            } else {
                // If it's a single image URL
                this.images = new ArrayList<>();
                this.images.add(imageJson);
            }
        } catch (Exception e) {
            this.images = new ArrayList<>();
            this.images.add(imageJson); // Treat as single URL if JSON parsing fails
        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
} 