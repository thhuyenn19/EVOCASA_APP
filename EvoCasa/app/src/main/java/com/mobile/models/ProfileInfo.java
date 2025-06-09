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
