package com.thanhhuyen.models;

public class ShippingAddress {
    private String name;
    private String phone;
    private String address;
    private boolean isDefault;

    public ShippingAddress() {}

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public boolean isDefault() { return isDefault; }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

}
