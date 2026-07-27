package com.example.messmate.common.models;

public class MoreOption {
    private String title;
    private String subtitle;
    private int iconResId;

    public MoreOption(String title, String subtitle, int iconResId) {
        this.title = title;
        this.subtitle = subtitle;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getIconResId() {
        return iconResId;
    }
}
