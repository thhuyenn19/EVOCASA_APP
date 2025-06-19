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
    private String image;  // là JSON String từ Firebase

    // constructor rỗng cần có cho Firebase
    public FlashSaleProduct() {}

    @PropertyName("Name")
    public String getName() {
        return name;
    }

    @PropertyName("Price")
    public double getPrice() {
        return price;
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
}
