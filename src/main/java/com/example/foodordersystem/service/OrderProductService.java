package com.example.foodordersystem.service;

import com.example.foodordersystem.model.Order;
import com.example.foodordersystem.model.OrderProduct;
import com.example.foodordersystem.repository.OrderProductRepository;

import java.util.List;

public class OrderProductService {


    private OrderProductRepository orderProductRepository;

    public List<Order> getPendingOrders() {
        return orderProductRepository.getPendingOrders();
    }

    public Order getOrderDetails(int orderId) {
        return orderProductRepository.getOrderById(orderId);
    }

}
