package com.mobile.models;

public class EventItem extends TimelineItem {
    private String time;
    private String desc;
    private boolean isActive;

    public EventItem(String time, String desc, boolean isActive) {
        this.time = time;
        this.desc = desc;
        this.isActive = isActive;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
