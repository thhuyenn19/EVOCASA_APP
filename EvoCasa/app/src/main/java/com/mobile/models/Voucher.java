package com.mobile.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class Voucher {

    @Exclude
    private String id;

    @PropertyName("Name")
    private String name;

    @PropertyName("DiscountPercent")
    private double discountPercent;

    @PropertyName("Maximum threshold")
    private double maxDiscount;

    @PropertyName("Minimum order value")
    private double minOrderValue;

    @PropertyName("ExpireDate")
    private Timestamp expireDate;

    public Voucher() {}

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("Name")
    public String getName() {
        return name;
    }

    @PropertyName("Name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("DiscountPercent")
    public double getDiscountPercent() {
        return discountPercent;
    }

    @PropertyName("DiscountPercent")
    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    @PropertyName("Maximum threshold")
    public double getMaxDiscount() {
        return maxDiscount;
    }

    @PropertyName("Maximum threshold")
    public void setMaxDiscount(double maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    @PropertyName("MinOrderValue")
    public double getMinOrderValue() {
        return minOrderValue;
    }

    @PropertyName("Minimum order value")
    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    @PropertyName("ExpireDate")
    public Timestamp getExpireDate() {
        return expireDate;
    }

    @PropertyName("ExpireDate")
    public void setExpireDate(Timestamp expireDate) {
        this.expireDate = expireDate;
    }

    // Logic để kiểm tra có còn dùng được hay không
    public boolean isValid(double orderTotal) {
        return orderTotal >= minOrderValue && expireDate != null && expireDate.toDate().getTime() > System.currentTimeMillis();
    }
}
