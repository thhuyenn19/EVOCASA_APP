package com.mobile.models;

public class OrderItem {
    private int imageResId;
    private String title;
    private int price;
    private int quantity;

    public OrderItem() {
    }

    public OrderItem(int imageResId, String title, int price, int quantity) {
        this.imageResId = imageResId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
