package com.mobile.models;

import com.google.firebase.firestore.PropertyName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class FlashSaleProduct {
    @PropertyName("Name")
    private String name;

    @PropertyName("Price")
    private double price;

    @PropertyName("Image")
    private String image;  // JSON String from Firebase

    @com.google.firebase.firestore.Exclude
    private String id;

    // Empty constructor required for Firebase
    public FlashSaleProduct() {}

    @PropertyName("Name")
    public String getName() {
        return name;
    }

    @PropertyName("Name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("Price")
    public double getPrice() {
        return price;
    }

    @PropertyName("Price")
    public void setPrice(double price) {
        this.price = price;
    }

    @PropertyName("Image")
    public String getImage() {
        return image;
    }

    @PropertyName("Image")
    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getImageList() {
        if (image == null) return new ArrayList<>();
        try {
            if (image.startsWith("[")) {
                // If it's a JSON array string
                return new Gson().fromJson(image, new TypeToken<List<String>>() {}.getType());
            } else {
                // If it's a single URL
                List<String> list = new ArrayList<>();
                list.add(image);
                return list;
            }
        } catch (Exception e) {
            List<String> list = new ArrayList<>();
            list.add(image); // Add as single URL if parsing fails
            return list;
        }
    }

    public String getFirstImage() {
        List<String> list = getImageList();
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    @com.google.firebase.firestore.Exclude
    public String getId() { return id; }

    @com.google.firebase.firestore.Exclude
    public void setId(String id) { this.id = id; }
}
