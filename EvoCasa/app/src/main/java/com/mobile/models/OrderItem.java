package com.mobile.models;

public class OrderItem {
    private String imageUrl;

    private String title;
    private long price;
    private int quantity;

    public OrderItem() {
    }

    public OrderItem(String imageUrl, String title, long price, int quantity) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
