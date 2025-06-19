package com.mobile.models;

public class OrderDetail {
    String shippingMethod;
    String paymentMethod;
    String note;

    public OrderDetail() {
    }

    public OrderDetail(String shippingMethod, String paymentMethod, String note) {
        this.shippingMethod = shippingMethod;
        this.paymentMethod = paymentMethod;
        this.note = note;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
