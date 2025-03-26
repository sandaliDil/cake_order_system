package com.example.foodordersystem.model;

public class OrderProduct {
    private int orderId;
    private int productId;
    private double quantity;

    public OrderProduct(int orderId, double quantity, int productId) {
        this.orderId = orderId;
        this.quantity = quantity;
        this.productId = productId;
    }

    public OrderProduct() {

    }

    public OrderProduct(int productId, double quantity) {

    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

}
