package com.mobile.models;

public class SubCategory {
    private String name;
    private boolean isSelected;

    public SubCategory(String name, boolean isSelected) {
        this.name = name;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
