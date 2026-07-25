package com.example.messmate.models;


public class OrderItem {
    private String orderId;
    private String messName;
    private String planType;
    private String mealType; // Lunch / Dinner
    private String status;   // Confirmed / Preparing / Active
    private String time;

    public OrderItem(String orderId, String messName, String planType, String mealType, String status, String time) {
        this.orderId = orderId;
        this.messName = messName;
        this.planType = planType;
        this.mealType = mealType;
        this.status = status;
        this.time = time;
    }

    public String getOrderId() { return orderId; }
    public String getMessName() { return messName; }
    public String getPlanType() { return planType; }
    public String getMealType() { return mealType; }
    public String getStatus() { return status; }
    public String getTime() { return time; }
}