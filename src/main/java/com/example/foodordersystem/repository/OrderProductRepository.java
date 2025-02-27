package com.example.foodordersystem.repository;

import com.example.foodordersystem.database.DatabaseConnection;
import com.example.foodordersystem.model.Order;
import com.example.foodordersystem.model.OrderProduct;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class OrderProductRepository {


    public List<Order> getPendingOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE status = 0";  // status = 0 means incomplete orders

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Order order = new Order();
                order.setId(resultSet.getInt("id"));
                order.setBranchId(resultSet.getInt("branch_id"));
                order.setUserId(resultSet.getInt("user_id"));
                order.setOrderDate(resultSet.getDate("order_date").toLocalDate().atStartOfDay());
                order.setOption(resultSet.getString("option"));
                order.setStatus(resultSet.getBoolean("status"));

                // Fetch order items separately

                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    private static final String DB_URL = "jdbc:mysql://localhost:3306/productorder";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "admin";

    public List<OrderProduct> getOrderProducts(int orderId) {
        List<OrderProduct> products = new ArrayList<>();
        String query = "SELECT * FROM order_product WHERE order_id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                products.add(new OrderProduct(
                        resultSet.getInt("order_id"),
                        resultSet.getDouble("quantity"),
                        resultSet.getInt("product_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Order getOrderById(int orderId) {
        String query = "SELECT * FROM orders WHERE id = ?";
        Order order = null;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                List<OrderProduct> items = getOrderProducts(orderId);
                order = new Order(
                                        resultSet.getInt("id"),
                                        resultSet.getInt("branchId"),
                                        resultSet.getInt("userId"),
                                        resultSet.getString("userName"),
                                        resultSet.getDate("orderDate").toLocalDate(),
                                        resultSet.getString("option"),
                        resultSet.getBoolean("status"),
                        resultSet.getString("timeRange"),
                        items
                                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }



}
