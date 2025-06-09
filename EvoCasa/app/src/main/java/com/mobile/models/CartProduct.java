package com.mobile.models;

public class CartProduct {
    private String title;
    private double price;
    private int imageResId;
    private int quantity;
    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


    public CartProduct(boolean isSelected) {
        this.isSelected = isSelected;
    }



    public CartProduct(String title, double price, int imageResId, int quantity) {
        this.title = title;
        this.price = price;
        this.imageResId = imageResId;
        this.quantity = quantity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
