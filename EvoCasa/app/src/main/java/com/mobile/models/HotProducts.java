package com.mobile.models;

public class HotProducts {
    private int imageResId;
    private String name;
    private String oldPrice;
    private String newPrice;
    private String discount;
    private float rating;

    public HotProducts(int imageResId, String name, String oldPrice, String newPrice, String discount, float rating) {
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
