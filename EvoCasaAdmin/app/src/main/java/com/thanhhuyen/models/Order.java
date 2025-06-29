package com.thanhhuyen.models;

import java.util.Map;

public class Order {
    private Map<String, String> OrderDate;
    private String TrackingNumber;
    private String Status;
    private int TotalPrice;
    private Map<String, String> Customer_id;
    private String CustomerName; // resolved từ collection Customers

    public Order() {}

    public Map<String, String> getOrderDate() { return OrderDate; }
    public void setOrderDate(Map<String, String> orderDate) { OrderDate = orderDate; }

    public String getTrackingNumber() { return TrackingNumber; }
    public void setTrackingNumber(String trackingNumber) { TrackingNumber = trackingNumber; }

    public String getStatus() { return Status; }
    public void setStatus(String status) { Status = status; }

    public int getTotalPrice() { return TotalPrice; }
    public void setTotalPrice(int totalPrice) { TotalPrice = totalPrice; }

    public Map<String, String> getCustomer_id() { return Customer_id; }
    public void setCustomer_id(Map<String, String> customer_id) { Customer_id = customer_id; }

    public String getCustomerName() { return CustomerName; }
    public void setCustomerName(String customerName) { CustomerName = customerName; }
}