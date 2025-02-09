package com.example.foodordersystem.service;

import com.example.foodordersystem.model.Order;
import com.example.foodordersystem.model.Product;
import com.example.foodordersystem.repository.OrderRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService() {
        this.orderRepository = new OrderRepository();
    }

    public int saveOrder(Order order) {
        return orderRepository.saveOrder(order);
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

    public boolean checkOrderExists(int branchId, LocalDate orderDate) {
        return orderRepository.checkOrderExists(branchId, orderDate);
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
}
