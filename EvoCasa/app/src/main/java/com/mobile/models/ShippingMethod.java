package com.mobile.models;

import androidx.annotation.DrawableRes;

public class ShippingMethod {
    private final String name;
    private final String receiveOn;
    private final double price;
    private final @DrawableRes int iconRes;

    public ShippingMethod(String name, double price, String receiveOn, @DrawableRes int iconRes) {
        this.name      = name;
        this.price     = price;
        this.receiveOn = receiveOn;
        this.iconRes   = iconRes;
    }

    public String getName()     { return name; }
    public String getReceiveOn(){ return receiveOn; }
    public double getPrice()    { return price; }
    public @DrawableRes int getIconRes() { return iconRes; }
}
