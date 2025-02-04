package com.example.foodordersystem.repository;

import com.example.foodordersystem.database.DatabaseConnection;
import com.example.foodordersystem.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        String query = "SELECT * FROM products ORDER BY id ASC";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getInt("id"));
                product.setProductCode(resultSet.getString("product_code"));
                product.setProductName(resultSet.getString("product_name"));
                productList.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging
        }

        return productList;
    }

    public Product findById(int productId) {
        String query = "SELECT * FROM products WHERE id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, productId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getInt("id"));
                product.setProductCode(resultSet.getString("product_code"));
                product.setProductName(resultSet.getString("product_name"));
                return product;
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging
        }

        return null; // Return null if no product is found
    }

    public boolean saveProduct(Product product) {
        String query = "INSERT INTO products (product_code, product_name) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, product.getProductCode());
            preparedStatement.setString(2, product.getProductName());
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1)); // Set generated ID to the product object
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging
        }

        return false; // Return false if save operation fails
    }

    public boolean updateProduct(Product product) {
        String query = "UPDATE products SET product_code = ?, product_name = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, product.getProductCode());
            preparedStatement.setString(2, product.getProductName());
            preparedStatement.setInt(3, product.getId());
            int affectedRows = preparedStatement.executeUpdate();

            return affectedRows > 0; // Return true if update was successful
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging
        }

        return false; // Return false if update operation fails
    }

    public boolean deleteProduct(int productId) {
        String query = "DELETE FROM products WHERE id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, productId);
            int affectedRows = preparedStatement.executeUpdate();

            return affectedRows > 0; // Return true if delete was successful
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging
        }

        return false; // Return false if delete operation fails
    }

    public int getProductCount() {
        String query = "SELECT COUNT(*) AS productCount FROM products";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("productCount"); // Retrieve the count from result set
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging
        }
        return 0; // Return 0 if query fails
    }

}
