package com.mobile.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HotProducts {
    private String image;  // Firestore trả về String, không phải List
    private String name;
    private String oldPrice;
    private String newPrice;
    private String discount;
    private float rating;

    public HotProducts() {
        // Default constructor
    }

    public HotProducts(String image, String name, String oldPrice, String newPrice, String discount, float rating) {
        this.image = image;
        this.name = name;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.discount = discount;
        this.rating = rating;
    }

    // ✅ Chuyển từ JSON String sang List<String>
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

    public String getRandomImage() {
        List<String> imgs = getImageList();
        if (imgs != null && !imgs.isEmpty()) {
            Random random = new Random();
            return imgs.get(random.nextInt(imgs.size()));
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getOldPrice() {
        return oldPrice;
    }

    public String getNewPrice() {
        return newPrice;
    }

    public String getDiscount() {
        return discount;
    }

    public float getRating() {
        return rating;
    }
}
