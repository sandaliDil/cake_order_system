package com.example.foodordersystem.repository;

import com.example.foodordersystem.database.DatabaseConnection;
import com.example.foodordersystem.model.OrderProduct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderProductRepository {

    // Existing method to fetch products by orderId
    public List<OrderProduct> getProductsByOrderId(int orderId) {
        List<OrderProduct> orderProductList = new ArrayList<>();
        String query = "SELECT * FROM OrderItem WHERE order_id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                OrderProduct orderProduct = new OrderProduct();
                orderProduct.setOrderId(resultSet.getInt("order_id"));
                orderProduct.setProductId(resultSet.getInt("product_id"));
                orderProduct.setQuantity(resultSet.getDouble("quantity"));
                orderProductList.add(orderProduct);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging
        }

        return orderProductList;
    }

    // New method to search and update quantity by orderId
    public boolean searchAndUpdateQuantity(int orderId, int productId, double newQuantity) {
        String query = "UPDATE OrderItem SET quantity = ? WHERE order_id = ? AND product_id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameters for the prepared statement
            preparedStatement.setDouble(1, newQuantity);
            preparedStatement.setInt(2, orderId);
            preparedStatement.setInt(3, productId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0; // Return true if the update was successful
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging
        }

        return false; // Return false if the update operation fails
    }
}
