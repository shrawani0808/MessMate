package com.example.messmate.common.models;

public class OrderItem {
    private String orderId;
    private String messName;
    private String planType;
    private String mealType;
    private String status;
    private String time;
    private String userEmail;
    private String price;

    // Full constructor including all fields for DB & Adapter
    public OrderItem(String orderId, String messName, String planType, String mealType, String status, String time, String userEmail, String price) {
        this.orderId = orderId;
        this.messName = messName;
        this.planType = planType;
        this.mealType = mealType;
        this.status = status;
        this.time = time;
        this.userEmail = userEmail;
        this.price = price;
    }

    // Constructor matching the adapter fields
    public OrderItem(String orderId, String messName, String planType, String mealType, String status, String time) {
        this.orderId = orderId;
        this.messName = messName;
        this.planType = planType;
        this.mealType = mealType;
        this.status = status;
        this.time = time;
    }

    // Getters matching OrderAdapter requirements
    public String getOrderId() {
        return orderId;
    }

    public String getMessName() {
        return messName;
    }

    public String getPlanType() {
        return planType;
    }

    public String getMealType() {
        return mealType;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getPrice() {
        return price;
    }

    // Setters
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setMessName(String messName) {
        this.messName = messName;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}