package com.mobile.models;

import com.google.firebase.firestore.PropertyName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class WishProduct {

    @com.google.firebase.firestore.Exclude
    private String id;
    @PropertyName("Image")
    private String image;  // là JSON String từ Firebase

    @PropertyName("Name")
    private String name;

    @PropertyName("Price")
    private double price;

    // Vì không có rating trong Firestore → sẽ random trong Adapter
    private float rating;

    private boolean outOfStock;

    public boolean isOutOfStock() {
        return outOfStock;
    }

    public void setOutOfStock(boolean outOfStock) {
        this.outOfStock = outOfStock;
    }

    public WishProduct() {}

    @PropertyName("Name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("Price")
    public void setPrice(double price) {
        this.price = price;
    }

    // Getter
    @PropertyName("Name")
    public String getName() { return name; }

    @PropertyName("Price")
    public double getPrice() { return price; }

    public float getRating() { return rating; }

    public void setRating(float rating) { this.rating = rating; }

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

    @com.google.firebase.firestore.Exclude
    public String getId() { return id; }

    @com.google.firebase.firestore.Exclude
    public void setId(String id) { this.id = id; }
}








//BÀI CŨ
//package com.mobile.models;
//
//public class WishProduct {
//    private int imageResId;
//    private String name;
//    private String oldPrice;
//    private String newPrice;
//    private String discount;
//    private float rating;
//
//    public WishProduct(int imageResId, String name, String oldPrice, String newPrice, String discount, float rating) {
//        this.imageResId = imageResId;
//        this.name = name;
//        this.oldPrice = oldPrice;
//        this.newPrice = newPrice;
//        this.discount = discount;
//        this.rating = rating;
//    }
//
//    public int getImageResId() {
//        return imageResId;
//    }
//
//    public void setImageResId(int imageResId) {
//        this.imageResId = imageResId;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getOldPrice() {
//        return oldPrice;
//    }
//
//    public void setOldPrice(String oldPrice) {
//        this.oldPrice = oldPrice;
//    }
//
//    public String getNewPrice() {
//        return newPrice;
//    }
//
//    public void setNewPrice(String newPrice) {
//        this.newPrice = newPrice;
//    }
//
//    public String getDiscount() {
//        return discount;
//    }
//
//    public void setDiscount(String discount) {
//        this.discount = discount;
//    }
//
//    public float getRating() {
//        return rating;
//    }
//
//    public void setRating(float rating) {
//        this.rating = rating;
//    }
//}
