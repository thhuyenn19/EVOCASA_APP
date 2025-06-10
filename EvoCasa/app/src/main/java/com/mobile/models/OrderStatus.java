package com.mobile.models;

public class OrderStatus {
    private final String title;
    private boolean isSelected;

    public OrderStatus(String title, boolean isSelected) {
        this.title = title;
        this.isSelected = isSelected;
    }

    public String getTitle() { return title; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}

