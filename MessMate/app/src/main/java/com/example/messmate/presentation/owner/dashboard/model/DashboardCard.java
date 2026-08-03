package com.example.messmate.presentation.owner.dashboard.model;

public class DashboardCard {

    private int icon;
    private String title;
    private String value;

    public DashboardCard(int icon, String title, String value) {
        this.icon = icon;
        this.title = title;
        this.value = value;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }
}