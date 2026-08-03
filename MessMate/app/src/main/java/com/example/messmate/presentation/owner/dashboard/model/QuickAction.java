package com.example.messmate.presentation.owner.dashboard.model;

public class QuickAction {

    private int icon;
    private String title;

    public QuickAction(int icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }
}