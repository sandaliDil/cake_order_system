package com.example.foodordersystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class Order {
    private int id;
    private int branchId;
    private int userId;
    private String userName;
    private LocalDate orderDate;
    private String option;
    private boolean status;
    private String timeRange;

    public Order(int id, List<OrderProduct> items, boolean status, String option, LocalDate orderDate, String userName, int userId, int branchId) {
        this.id = id;
        this.items = items;
        this.status = status;
        this.option = option;
        this.orderDate = LocalDate.from(orderDate.atStartOfDay());
        this.userName = userName;
        this.userId = userId;
        this.branchId = branchId;
    }

    private List<OrderProduct> items;

    public Order() {
    }

    public Order(int id, int branchId, int userId, String userName, LocalDate orderDate, LocalDate orderDate1, String option, String timeRange, boolean status) {
    }


    public boolean isStatus() {
        return status;
    }

    public Order(int id, int branchId, int userId, String userName, LocalDate orderDate, String option, boolean status,
                 String timeRange, List<OrderProduct> items) {
        this.id = id;
        this.branchId = branchId;
        this.userId = userId;
        this.userName = userName;
        this.orderDate = orderDate;
        this.option = option;
        this.status = status;
        this.timeRange = timeRange;
        this.items = items;
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

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = LocalDate.from(orderDate);
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

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }
}
