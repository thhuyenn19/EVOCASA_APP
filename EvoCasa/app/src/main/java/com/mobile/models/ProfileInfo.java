package com.mobile.models;

public class ProfileInfo {
    private String label;
    private String value;
    private int iconRes;

    public ProfileInfo(String label, String value, int iconRes) {
        this.label = label;
        this.value = value;
        this.iconRes = iconRes;
    }

    // Remove the incomplete constructor or fix it properly
    // The current constructor with (String phone, String phone1) doesn't initialize fields
    // If you need it, implement it properly:
    /*
    public ProfileInfo(String phone, String phone1) {
        this.label = "Phone";
        this.value = phone;
        this.iconRes = R.drawable.ic_phone; // or appropriate default
    }
    */

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public int getIconRes() {
        return iconRes;
    }
}