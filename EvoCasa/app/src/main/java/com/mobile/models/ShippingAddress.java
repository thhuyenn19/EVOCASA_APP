package com.mobile.models;

public class ShippingAddress {
    private String name;
    private String phone;
    private String address;
    private boolean isDefault;

    public ShippingAddress(String name, String phone, String address, boolean isDefault) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.isDefault = isDefault;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public boolean isDefault() { return isDefault; }
}
