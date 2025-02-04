package com.example.foodordersystem.controller;

import com.example.foodordersystem.model.Product;
import com.example.foodordersystem.service.BranchService;
import com.example.foodordersystem.service.OrderService;
import com.example.foodordersystem.service.ProductService;
import com.example.foodordersystem.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public class DashboardController {

    // FXML injected components
    @FXML
    private Button addUser;

    @FXML
    private Label userCountLabel;
    @FXML
    private Label orderCountLabel;
    @FXML
    private Label productCountLabel;
    @FXML
    private Label branchCountLabel;
    @FXML
    private VBox barChartContainer;
    @FXML
    private VBox pieChartContainer;

    // Service instances
    private final BranchService branchService;
    private final ProductService productService;
    private final OrderService orderService;
    private UserService userService;

    // Constructor to initialize services
    public DashboardController() {
        this.orderService = new OrderService();
        this.productService = new ProductService();
        this.branchService = new BranchService();
    }

    // Initialize method to load data
    @FXML
    public void initialize() {
        userService = new UserService();
        updateUserCount();
        updateOrderCount();
        updateProductCount();
        updateBranchCount();
        displayUserOrderChart();
        displayOrdersPerProductDailyChart();
    }

    // Display a bar chart for orders per product daily
    private void displayOrdersPerProductDailyChart() {
        // Fetch daily orders per product
        Map<LocalDate, Map<Product, Integer>> ordersPerDay = orderService.getOrdersPerProductDaily();

        // Create axes for the chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Order Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Quantity");

        // Create the bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Orders per Product Daily");

        // Prepare data series for the bar chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Product Orders");

        // Populate the series with data
        for (LocalDate orderDate : ordersPerDay.keySet()) {
            Map<Product, Integer> productOrders = ordersPerDay.get(orderDate);
            String orderDateString = orderDate.toString();  // Date for X-axis

            for (Product product : productOrders.keySet()) {
                int orderCount = productOrders.get(product);
                series.getData().add(new XYChart.Data<>(orderDateString, orderCount));
            }
        }

        // Add the series to the chart and display it
        barChart.getData().add(series);
        barChartContainer.getChildren().clear();
        barChartContainer.getChildren().add(barChart);
    }

    // Display a pie chart for user order counts
    private void displayUserOrderChart() {
        // Get order count per user
        Map<String, Integer> userOrderCounts = orderService.getOrderCountPerUser();

        // Prepare pie chart data
        PieChart.Data[] pieData = userOrderCounts.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()))
                .toArray(PieChart.Data[]::new);

        // Create the pie chart
        PieChart pieChart = new PieChart();
        pieChart.getData().setAll(pieData);

        // Display the chart in the container
        pieChartContainer.getChildren().add(pieChart);
    }

    // Update branch count on the UI
    private void updateBranchCount() {
        int branchCount = branchService.getBranchCount();
        branchCountLabel.setText("0" + branchCount);
    }

    // Update user count on the UI
    private void updateUserCount() {
        int userCount = userService.getUserCount();
        userCountLabel.setText("0" + userCount);
    }

    // Update order count on the UI
    private void updateOrderCount() {
        int orderCount = orderService.getOrderCount();
        orderCountLabel.setText("" + orderCount);
    }

    // Update product count on the UI
    private void updateProductCount() {
        int productCount = productService.getProductCount();
        productCountLabel.setText("0" + productCount);
    }

    @FXML
    private void handleBranch(ActionEvent event) {
        openWindow("/com/example/foodordersystem/Branch.fxml", "Register Branch");
    }

    @FXML
    private void handleOrders(ActionEvent event) {
        openWindow("/com/example/foodordersystem/OrderSummery.fxml", "Order Summery");
    }

    // Open the product registration view
    @FXML
    private void handleAddProduct(ActionEvent event) {
        openWindow("/com/example/foodordersystem/RegisterUser.fxml", "Add Product");
    }

    // Open the product view
    @FXML
    private void handleViewProduct(ActionEvent event) {
        openWindow("/com/example/foodordersystem/ProductView.fxml", "View Product");
    }

    // Helper method to open a new window
    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Close the application with confirmation
    @FXML
    private void closeButton(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved changes will be lost.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void logout(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/foodordersystem/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading login screen.");
        }
    }
}
