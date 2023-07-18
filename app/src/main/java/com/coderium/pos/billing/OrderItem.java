package com.coderium.pos.billing;


public class OrderItem {
    private String itemId;
    private String itemName;
    private String itemSize;
    private int quantity;
    private double price;

    public OrderItem() {
        // Empty constructor needed for Firebase
    }

    public OrderItem(String itemId, String itemName, String itemSize, int quantity, double price) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemSize = itemSize;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemSize() {
        return itemSize;
    }

    public void setItemSize(String itemSize) {
        this.itemSize = itemSize;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
