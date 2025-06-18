package com.mobile.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class HotProducts {
    private String image;

    @PropertyName("Name")
    private String name;

    @PropertyName("Price")
    private double price;

    // Vì không có rating trong Firestore → sẽ random trong Adapter
    private float rating;

    public HotProducts() {}

    // Getter
    @PropertyName("Name")
    public String getName() { return name; }

    @PropertyName("Price")
    public double getPrice() { return price; }

    public float getRating() { return rating; }

    public void setRating(float rating) { this.rating = rating; }

    public void setImage(String image) { this.image = image; }

    public List<String> getImageList() {
        try {
            return new Gson().fromJson(image, new TypeToken<List<String>>() {}.getType());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getFirstImage() {
        List<String> imgs = getImageList();
        return (imgs != null && !imgs.isEmpty()) ? imgs.get(0) : null;
    }
}
