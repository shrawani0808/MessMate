package com.example.messmate.common.models;
public class MessModel {
    private int id;
    private String name;
    private float rating;
    private int ratingCount;
    private String type; // e.g., "Pure Veg"
    private double pricePerMonth;
    private String location;

    public MessModel(int id, String name, float rating, int ratingCount, String type, double pricePerMonth, String location) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.type = type;
        this.pricePerMonth = pricePerMonth;
        this.location = location;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public float getRating() { return rating; }
    public int getRatingCount() { return ratingCount; }
    public String getType() { return type; }
    public double getPricePerMonth() { return pricePerMonth; }
    public String getLocation() { return location; }
}