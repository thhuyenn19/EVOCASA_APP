package com.mobile.models;

import java.util.List;

public class OrderGroup {
    private String status;
    private List<OrderItem> items;
    private boolean expanded;
    private long total;
    private String orderId;

    public OrderGroup() {
    }

    public OrderGroup(String status, List<OrderItem> items) {
        this.status = status;
        this.items = items;
        this.expanded = false; // mặc định chưa mở rộng
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
