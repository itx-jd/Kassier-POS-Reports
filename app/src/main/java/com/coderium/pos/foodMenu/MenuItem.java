package com.coderium.pos.foodMenu;

public class MenuItem {
    private String categoryId;
    private String itemDescription;
    private String itemId;
    private String itemName;
    private int itemPrice;
    private String itemSize;
    private String itemQuantity;

    // Required no-argument constructor
    public MenuItem() {
        // Empty constructor needed for Firebase
    }


    public MenuItem(String itemName, int itemPrice, String itemSize, String itemQuantity) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemSize = itemSize;
        this.itemQuantity = itemQuantity;
    }

    public MenuItem(String categoryId, String itemDescription, String itemId, String itemName, int itemPrice, String itemSize, String itemQuantity) {
        this.categoryId = categoryId;
        this.itemDescription = itemDescription;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemSize = itemSize;
        this.itemQuantity = itemQuantity;
    }

    public MenuItem(String categoryId, String itemDescription, String itemId, String itemName, int itemPrice, String itemSize) {
        this.categoryId = categoryId;
        this.itemDescription = itemDescription;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemSize = itemSize;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public String getItemSize() {
        return itemSize;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }
}
