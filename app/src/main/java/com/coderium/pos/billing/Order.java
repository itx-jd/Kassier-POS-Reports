package com.coderium.pos.billing;

import java.util.Map;

public class Order {
    private String orderId;
    private String orderDate;
    private double totalPrice;
    private double serviceCharges;

    private Map<String, OrderItem> items;

    public Order() {
        // Empty constructor needed for Firebase
    }


    public Order(String orderId, String orderDate, double totalPrice, double serviceCharges, Map<String, OrderItem> items) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.serviceCharges = serviceCharges;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(double serviceCharges) {
        this.serviceCharges = serviceCharges;
    }

    public Map<String, OrderItem> getItems() {
        return items;
    }

    public void setItems(Map<String, OrderItem> items) {
        this.items = items;
    }
}
