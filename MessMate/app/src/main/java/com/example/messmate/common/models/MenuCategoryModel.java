package com.example.messmate.common.models;



public class MenuCategoryModel {
    private String categoryName;
    private String itemsDescription;

    public MenuCategoryModel(String categoryName, String itemsDescription) {
        this.categoryName = categoryName;
        this.itemsDescription = itemsDescription;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getItemsDescription() {
        return itemsDescription;
    }
}
