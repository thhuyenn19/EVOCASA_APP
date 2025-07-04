package com.mobile.models;

public class SuggestedProducts {
    private int imageResId;
    private String name;
    private String oldPrice;
    private String newPrice;
    private String discount;
    private float rating;


    public SuggestedProducts(int imageResId, String name, String oldPrice, String newPrice, String discount, float rating) {
        this.imageResId = imageResId;
        this.name = name;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.discount = discount;
        this.rating = rating;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

    public String getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(String newPrice) {
        this.newPrice = newPrice;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
