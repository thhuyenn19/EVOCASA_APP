package com.mobile.models;

import com.google.firebase.firestore.PropertyName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class MightLike {

    @PropertyName("Name")
    private String name;

    @PropertyName("Price")
    private double price;

    @PropertyName("Image")
    private String image;

    private float rating;

    @com.google.firebase.firestore.Exclude
    private String id;

    public MightLike() {}

    @PropertyName("Name")
    public String getName() { return name; }

    @PropertyName("Price")
    public double getPrice() { return price; }

    @PropertyName("Image")
    public String getImage() { return image; }

    @PropertyName("Image")
    public void setImage(String image) { this.image = image; }

    public float getRating() { return rating; }

    public void setRating(float rating) { this.rating = rating; }

    public List<String> getImageList() {
        try {
            return new Gson().fromJson(image, new TypeToken<List<String>>() {}.getType());
        } catch (Exception e) {
            return new ArrayList<>();
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
