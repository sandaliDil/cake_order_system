package com.example.foodordersystem.repository;

import com.example.foodordersystem.database.DatabaseConnection;
import com.example.foodordersystem.model.Order;
import com.example.foodordersystem.model.OrderProduct;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Method to fetch order items for a given order ID
    private List<Map<String, Object>> getOrderProducts(int orderId, Connection connection) {
        List<Map<String, Object>> orderProducts = new ArrayList<>();

        String query = "SELECT op.id, op.order_id, op.product_id, op.quantity, op.price, p.product_name " +
                "FROM OrderItem op " +
                "JOIN Product p ON op.product_id = p.id " +
                "WHERE op.order_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, orderId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> orderProduct = new HashMap<>();
                    orderProduct.put("id", resultSet.getInt("id"));
                    orderProduct.put("order_id", resultSet.getInt("order_id"));
                    orderProduct.put("product_id", resultSet.getInt("product_id"));
                    orderProduct.put("quantity", resultSet.getDouble("quantity"));
                    orderProduct.put("price", resultSet.getDouble("price"));
                    orderProduct.put("product_name", resultSet.getString("product_name")); // Store product name

                    orderProducts.add(orderProduct);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orderProducts;
    }

    public ObservableList<OrderProduct> getOrderDetails(int orderId) {
        ObservableList<OrderProduct> orderProducts = FXCollections.observableArrayList();
        String query = "SELECT p.product_name AS productName, op.quantity, p.price " +
                "FROM OrderItem op " +
                "JOIN products p ON op.product_id = p.id " +
                "WHERE op.order_id = ? ";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                OrderProduct orderProduct = new OrderProduct();
               // orderProduct.(resultSet.getString("productName"));
                orderProduct.setQuantity(resultSet.getInt("quantity"));
                orderProducts.add(orderProduct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderProducts;
    }


    private static final String DB_URL = "jdbc:mysql://localhost:3306/productorder";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "admin";

//    public List<Order> getPendingOrders1() {
//        List<Order> orders = new ArrayList<>();
//        String query = "SELECT * FROM orders WHERE status = false";
//
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//             PreparedStatement statement = connection.prepareStatement(query);
//             ResultSet resultSet = statement.executeQuery()) {
//
//            while (resultSet.next()) {
//                int orderId = resultSet.getInt("id");
//                List<OrderProduct> items = getOrderProducts(orderId);
//                orders.add(new Order(
//                        orderId,
//                        resultSet.getInt("branchId"),
//                        resultSet.getInt("userId"),
//                        resultSet.getString("userName"),
//                        resultSet.getDate("orderDate").toLocalDate(),
//                        resultSet.getString("option"),
//                        resultSet.getTime("order_time").toLocalTime(), // Convert SQL Time to LocalTime
//                        items,
//                        resultSet.getBoolean("status")
//
//                ));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return orders;
//    }

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
