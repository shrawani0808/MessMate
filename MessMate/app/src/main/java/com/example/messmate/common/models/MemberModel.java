package com.example.messmate.common.models;
public class MemberModel {
    private String name;
    private String planType;
    private String status; // "Active" or "Inactive"
    private int imageResId; // Optional drawable resource or image URL string

    public MemberModel(String name, String planType, String status, int imageResId) {
        this.name = name;
        this.planType = planType;
        this.status = status;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getPlanType() {
        return planType;
    }

    public String getStatus() {
        return status;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }
}