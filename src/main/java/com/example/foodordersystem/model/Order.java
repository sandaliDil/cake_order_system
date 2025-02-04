package com.example.foodordersystem.model;

import java.time.LocalDate;
import java.util.List;

public class Order {
    private int id;
    private int branchId;
    private int userId;
    private String userName;
    private LocalDate orderDate;
    private String option;
    private boolean status;
    private List<OrderProduct> items;

    public Order() {
    }

    public Order(int id, int branchId, int userId, String userName, LocalDate orderDate, String option, List<OrderProduct> items, boolean status) {
        this.id = id;
        this.branchId = branchId;
        this.userId = userId;
        this.userName = userName;
        this.orderDate = orderDate;
        this.option = option;
        this.status=status;
        this.items = items;
    }

    public Order(int id, int branchId, int userId, String userName, LocalDate orderDate, String option, boolean status) {
    }

    public boolean isStatus(Boolean aBoolean) {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public List<OrderProduct> getItems() {
        return items;
    }

    public void setItems(List<OrderProduct> items) {
        this.items = items;
    }
}
