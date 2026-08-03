package com.example.messmate.presentation.owner.dashboard.model;

public class ActivityLog {

    private int icon;
    private String title;
    private String time;

    public ActivityLog(int icon, String title, String time) {
        this.icon = icon;
        this.title = title;
        this.time = time;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }
}