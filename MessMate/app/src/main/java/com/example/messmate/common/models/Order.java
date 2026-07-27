package com.example.messmate.common.models;

public class Order {
    private String orderId;
    private String customerName;
    private String mealType;
    private String items;
    private String time;
    private String amount;
    private String status; // "Pending", "Completed", or "Cancelled"

    public Order(String orderId, String customerName, String mealType, String items, String time, String amount, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.mealType = mealType;
        this.items = items;
        this.time = time;
        this.amount = amount;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getMealType() { return mealType; }
    public String getItems() { return items; }
    public String getTime() { return time; }
    public String getAmount() { return amount; }
    public String getStatus() { return status; }
}