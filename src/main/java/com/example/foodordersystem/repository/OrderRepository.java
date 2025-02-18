package com.example.foodordersystem.repository;

import com.example.foodordersystem.database.DatabaseConnection;
import com.example.foodordersystem.model.Order;
import com.example.foodordersystem.model.OrderProduct;
import com.example.foodordersystem.model.Product;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static com.example.foodordersystem.repository.OrderProductRepository.*;

public class OrderRepository {

    /**
     * Saves an order and its associated items into the database.
     *
     * @param order The order object containing order details and items.
     * @return The generated order ID, or -1 if saving failed.
     */
    public int saveOrder(Order order) {
        String orderQuery = "INSERT INTO Orders (user_id, branch_id, order_date, `option`, status, time_range) VALUES (?, ?, ?, ?, ?, ?)";
        String orderItemQuery = "INSERT INTO OrderItem (order_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Insert the order
            try (PreparedStatement orderStatement = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                orderStatement.setInt(1, order.getUserId());
                orderStatement.setInt(2, order.getBranchId());
                orderStatement.setDate(3, Date.valueOf(order.getOrderDate()));
                orderStatement.setString(4, order.getOption());
                orderStatement.setBoolean(5, order.isStatus(true)); // Add status field
                orderStatement.setString(6, order.getTimeRange()); // Add time_range field

                int rowsAffected = orderStatement.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = orderStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int orderId = generatedKeys.getInt(1);
                            order.setId(orderId);

                            // Save order items
                            saveOrderItems(orderItemQuery, connection, orderId, order.getItems());
                            connection.commit(); // Commit the transaction
                            return orderId; // Return the generated order ID
                        }
                    }
                }
                connection.rollback(); // Rollback if no rows were affected
            } catch (SQLException e) {
                connection.rollback(); // Rollback on failure
                logError("Error saving order", e);
            }
        } catch (SQLException e) {
            logError("Database connection error", e);
        }
        return -1; // Return -1 on failure
    }



    /**
     * Helper method to save order items.
     */
    private void saveOrderItems(String query, Connection connection, int orderId, List<OrderProduct> items) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (OrderProduct item : items) {
                statement.setInt(1, orderId);
                statement.setInt(2, item.getProductId());
                statement.setDouble(3, item.getQuantity());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public boolean updateOrder(Order order) {
        String updateOrderQuery = "UPDATE Orders SET user_id = ?, branch_id = ?, order_date = ?, `option` = ?, status = ?, time_range = ? WHERE id = ?";
        String deleteOrderItemsQuery = "DELETE FROM OrderItem WHERE order_id = ?";
        String insertOrderItemQuery = "INSERT INTO OrderItem (order_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Update the order details
            try (PreparedStatement orderStatement = connection.prepareStatement(updateOrderQuery)) {
                orderStatement.setInt(1, order.getUserId());
                orderStatement.setInt(2, order.getBranchId());
                orderStatement.setDate(3, Date.valueOf(order.getOrderDate()));
                orderStatement.setString(4, order.getOption());
                orderStatement.setBoolean(5, order.isStatus(true));
                orderStatement.setString(6, order.getTimeRange());
                orderStatement.setInt(7, order.getId());

                int rowsAffected = orderStatement.executeUpdate();
                if (rowsAffected == 0) {
                    connection.rollback(); // Rollback if no order is updated
                    return false;
                }
            }

            // Delete existing order items
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteOrderItemsQuery)) {
                deleteStatement.setInt(1, order.getId());
                deleteStatement.executeUpdate();
            }

            // Insert updated order items
            saveOrderItems(insertOrderItemQuery, connection, order.getId(), order.getItems());

            connection.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            logError("Error updating order", e);
            return false;
        }
    }


    public boolean checkOrderExistsInLastHour(int branchId) {
        String query = "SELECT COUNT(*) FROM Orders WHERE branch_id = ? AND order_time >= NOW() - INTERVAL 1 HOUR";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, branchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logError("Database error while checking for recent orders", e);
        }
        return false;
    }

    /**
     * Retrieves the total count of orders in the database.
     *
     * @return The total number of orders.
     */
    public int getOrderCount() {
        String query = "SELECT COUNT(*) AS orderCount FROM Orders";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("orderCount");
            }
        } catch (SQLException e) {
            logError("Error retrieving order count", e);
        }
        return 0; // Return 0 if the query fails
    }

    /**
     * Retrieves the count of orders placed by each user.
     *
     * @return A map where the key is the username and the value is the order count.
     */
    public Map<String, Integer> getOrderCountPerUser() {
        String query = "SELECT u.userName, COUNT(o.id) AS order_count " +
                "FROM Orders o JOIN users u ON o.user_id = u.id " +
                "GROUP BY u.userName";

        Map<String, Integer> orderCountMap = new HashMap<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                orderCountMap.put(resultSet.getString("userName"), resultSet.getInt("order_count"));
            }
        } catch (SQLException e) {
            logError("Error retrieving order count per user", e);
        }
        return orderCountMap;
    }

    /**
     * Retrieves the count of orders placed for each product on each day.
     *
     * @return A map where the key is the date, and the value is another map holding the product and its order count.
     */
    public Map<LocalDate, Map<Product, Integer>> getOrdersPerProductDaily() {
        String query = "SELECT o.order_date, oi.product_id, p.product_name, SUM(oi.quantity) AS total_quantity " +
                "FROM OrderItem oi " +
                "JOIN Orders o ON oi.order_id = o.id " +
                "JOIN products p ON oi.product_id = p.id " +
                "GROUP BY o.order_date, oi.product_id, p.product_name " +
                "ORDER BY o.order_date";

        Map<LocalDate, Map<Product, Integer>> ordersPerDay = new HashMap<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                LocalDate orderDate = resultSet.getDate("order_date").toLocalDate();
                Product product = new Product(resultSet.getInt("product_id"), resultSet.getString("product_name"));
                int totalQuantity = resultSet.getInt("total_quantity");

                ordersPerDay.putIfAbsent(orderDate, new HashMap<>());
                ordersPerDay.get(orderDate).put(product, totalQuantity);
            }
        } catch (SQLException e) {
            logError("Error retrieving daily orders per product", e);
        }
        return ordersPerDay;
    }

    /**
     * Retrieves all orders from the database.
     *
     * @return A list of all orders.
     */
    public List<Order> getAllOrders() {
        String query = "SELECT o.id, o.user_id, u.userName, o.branch_id, o.order_date, o.option " +
                "FROM Orders o JOIN users u ON o.user_id = u.id";

        List<Order> orders = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                orders.add(buildOrder(resultSet));
            }
        } catch (SQLException e) {
            logError("Error retrieving all orders", e);
        }
        return orders;
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId The ID of the order to search.
     * @return A list of orders matching the given ID.
     */
    public List<Order> getOrdersById(int orderId) {
        String query = " SELECT o.id, o.user_id, u.userName, o.branch_id, o.order_date, o.option " +
                "FROM Orders o JOIN users u ON o.user_id = u.id " +
                "WHERE o.id = ?";

        List<Order> orders = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                orders.add(buildOrder(resultSet));
            }
        } catch (SQLException e) {
            logError("Error retrieving orders by ID", e);
        }
        return orders;
    }

    /**
     * Helper method to construct an Order object from a ResultSet.
     */
    private Order buildOrder(ResultSet resultSet) throws SQLException {
        return new Order(
                        resultSet.getInt("id"),
                        resultSet.getInt("branch_id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("userName"),
                        resultSet.getDate("order_date").toLocalDate(),
                resultSet.getDate("order_date").toLocalDate(),
                resultSet.getString("option"),
                resultSet.getString("time_range"),
                resultSet.getBoolean("status")
                );
    }

    /**
     * Logs SQL-related errors for debugging purposes.
     */
    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    public Map<String, Map<String, Map<String, Double>>> getOrderDetailsByDateAndOptionWithUser(LocalDate date, String option) {
        // Updated SQL query to include user filtering based on branch
        String query = "SELECT b.branch_name, u.username, p.product_name, SUM(oi.quantity) AS total_quantity " +
                "FROM Orders o " +
                "JOIN OrderItem oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "JOIN branches b ON o.branch_id = b.id " +
                "JOIN users u ON o.user_id = u.id " +
                "WHERE o.order_date = ? AND o.option = ? " +
                "GROUP BY b.branch_name, p.product_name, p.id " +
                "ORDER BY p.id ASC";

        Map<String, Map<String, Map<String, Double>>> branchUserProductMap = new LinkedHashMap<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set query parameters
            statement.setDate(1, Date.valueOf(date));
            statement.setString(2, option);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String branchName = resultSet.getString("branch_name");
                String userName = resultSet.getString("username");
                String productName = resultSet.getString("product_name");
                double quantity = resultSet.getDouble("total_quantity");

                // Organizing data in a nested map
                branchUserProductMap
                        .computeIfAbsent(branchName, k -> new LinkedHashMap<>())  // Branch level
                        .computeIfAbsent(userName, k -> new LinkedHashMap<>())   // User level
                        .put(productName, quantity);                             // Product and quantity level
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return an unmodifiable map to ensure no external changes
        return Collections.unmodifiableMap(branchUserProductMap);
    }


    public Map<String, Map<String, Double>> getOrderDetailsByDateAndOption(LocalDate date, String option) {
        // SQL query with added ORDER BY for product_id
        String query = "SELECT b.branch_name, p.product_name, SUM(oi.quantity) AS total_quantity " +
                "FROM Orders o " +
                "JOIN OrderItem oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "JOIN branches b ON o.branch_id = b.id " +
                "WHERE o.order_date = ? AND o.option = ? " +
                "GROUP BY b.branch_name, p.product_name, p.id " +
                "ORDER BY p.id ASC";

        Map<String, Map<String, Double>> branchProductMap = new LinkedHashMap<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the parameters for both the date and the option
            statement.setDate(1, Date.valueOf(date));
            statement.setString(2, option);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String branchName = resultSet.getString("branch_name");
                String productName = resultSet.getString("product_name");
                double quantity = resultSet.getDouble("total_quantity");

                // Add the data to the map
                branchProductMap
                        .computeIfAbsent(branchName, k -> new LinkedHashMap<>())
                        .put(productName, quantity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Return an unmodifiable map to ensure no changes can be made
        return Collections.unmodifiableMap(branchProductMap);
    }


    /**
     * Fetch orders filtered by branch ID and order date.
     *
     * @param branchId   The ID of the branch to filter orders.
     * @param orderDate  The specific date to filter orders.
     * @return A list of filtered orders.
     */
    public List<Order> getOrdersByBranchAndDate(int branchId, LocalDate orderDate) {
        List<Order> orderList = new ArrayList<>();
        String query = "SELECT o.id, o.branch_id, o.user_id, u.name AS user_name, o.order_date, o.option " +
                "FROM   o  Orders o " +
                "JOIN users u ON o.user_id = u.id " +
                "WHERE o.branch_id = ? AND o.order_date = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set query parameters
            preparedStatement.setInt(1, branchId);
            preparedStatement.setDate(2, Date.valueOf(orderDate));

            // Execute query and process results
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Order order = new Order();
                order.setId(resultSet.getInt("id"));
                order.setBranchId(resultSet.getInt("branch_id"));
                order.setUserId(resultSet.getInt("user_id"));
                order.setUserName(resultSet.getString("user_name"));
                order.setOrderDate(resultSet.getDate("order_date").toLocalDate().atStartOfDay());
                order.setOption(resultSet.getString("option"));

                orderList.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging in production
        }

        return orderList;
    }

    /**
     * Retrieves a single order by its ID.
     *
     * @param orderId The ID of the order to fetch.
     * @return The order object if found, otherwise null.
     */
    public Order getOrderById(int orderId) {
        String query = "SELECT o.id, o.user_id, u.userName, o.branch_id, o.order_date, o.option, o.status, o.time_range " +
                "FROM Orders o JOIN users u ON o.user_id = u.id " +
                "WHERE o.id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return buildOrder(resultSet); // Reuse the buildOrder method to map the result.
            }
        } catch (SQLException e) {
            logError("Error retrieving order by ID", e);
        }
        return null; // Return null if the order is not found or an error occurs.
    }

    public boolean checkOrderExists(int branchId, LocalDate orderDate, String timeRange) {
        String query = "SELECT COUNT(*) FROM Orders WHERE branch_id = ? AND order_date = ? AND time_range = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, branchId);
            preparedStatement.setDate(2, Date.valueOf(orderDate)); // Convert LocalDate to SQL Date
            preparedStatement.setString(3, timeRange); // Check for the specific time range

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // If count > 0, an order exists
                }
            }
        } catch (SQLException e) {
            logError("Error checking order existence", e);
        }
        return false; // Return false if no order exists
    }

    // Assuming you are using a database or an in-memory storage.
    private static final List<Order> orders = new ArrayList<>();  // This is a placeholder for the actual database.

    /**
     * Save or update an Order in the repository.
     * This method also saves or updates the associated OrderProducts.
     * @param order The Order object to save or update.
     */
    public void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        // Check if the order exists already (based on order ID).
        Order existingOrder = getOrderById(order.getId());

        if (existingOrder == null) {
            // New order, add it to the orders list
            orders.add(order);
        } else {
            // Existing order, update it
            existingOrder.setBranchId(order.getBranchId());
            existingOrder.setUserId(order.getUserId());
            existingOrder.setUserName(order.getUserName());
          //  existingOrder.setOrderDate(order.getOrderDate());
            existingOrder.setOption(order.getOption());

            // Update the OrderProducts for the existing order
            for (OrderProduct newProduct : order.getItems()) {
                OrderProduct existingProduct = findOrderProductByProductId(existingOrder, newProduct.getProductId());
                if (existingProduct != null) {
                    existingProduct.setQuantity(newProduct.getQuantity());
                } else {
                    existingOrder.getItems().add(newProduct);  // Add new product if not found
                }
            }
        }
        // Persist the changes (this can be done with your ORM or database here).
        // If you are using a DB, you would use something like:
        // entityManager.persist(order);
    }


    /**
     * Finds an OrderProduct by its productId within the given order.
     * @param order The order to search within.
     * @param productId The ID of the product to find.
     * @return The OrderProduct if found, or null if not found.
     */
    private OrderProduct findOrderProductByProductId(Order order, int productId) {
        for (OrderProduct orderProduct : order.getItems()) {
            if (orderProduct.getProductId() == productId) {
                return orderProduct;
            }
        }
        return null;
    }

    public int countPendingOrders() {
        String query = "SELECT COUNT(*) FROM Orders WHERE status = 0";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting pending orders: " + e.getMessage());
        }
        return 0; // Return 0 if an error occurs
    }




    public List<OrderProduct> getProductsForOrder(int orderId) {
        List<OrderProduct> orderProducts = new ArrayList<>();
        String query = "SELECT p.id, p.product_name, op.quantity FROM OrderItem op " +
                "JOIN products p ON op.product_id = p.id WHERE op.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("id");
                double quantity = rs.getDouble("quantity");

                OrderProduct orderProduct = new OrderProduct(orderId, quantity, productId);
                orderProducts.add(orderProduct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderProducts;
    }

}
