package com.example.foodordersystem.service;

import com.example.foodordersystem.database.DatabaseConnection;
import com.example.foodordersystem.model.*;
import com.example.foodordersystem.repository.BranchRepository;
import com.example.foodordersystem.repository.OrderRepository;
import com.example.foodordersystem.repository.UserRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService() {
        this.orderRepository = new OrderRepository();
    }

    public int saveOrder(Order order) {
        return orderRepository.saveOrder(order);
    }

    public boolean updateOrder(Order order) {
        return orderRepository.updateOrder(order);
    }

    public int getOrderCount() {
        return orderRepository.getOrderCount();
    }

    public Map<String, Integer> getOrderCountPerUser() {
        return orderRepository.getOrderCountPerUser(); // Fetch the order count by user
    }

    public Map<LocalDate, Map<Product, Integer>> getOrdersPerProductDaily() {
        return orderRepository.getOrdersPerProductDaily();
    }

    public List<Order> getAllOrders() {
        return orderRepository.getAllOrders();
    }

    public boolean checkOrderExists(int branchId, LocalDate orderDate, String timeRange) {
        return orderRepository.checkOrderExists(branchId, orderDate, timeRange);
    }


    /**
     * Searches for orders by their ID.
     *
     * @param orderId the ID of the order to search for.
     * @return a list of orders matching the given ID.
     */
    public List<Order> searchOrdersById(int orderId) {
        return orderRepository.getOrdersById(orderId);
    }

    public Order getOrderById(int orderId) {
        return orderRepository.getOrderById(orderId);
    }

    public boolean updateOrderStatusAndOption(int orderId, int status, String option) {
        return orderRepository.updateStatusAndOption(orderId, status, option);
    }



}
