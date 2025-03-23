package com.example.foodordersystem.controller;

import com.example.foodordersystem.repository.OrderRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.*;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static java.lang.Double.MAX_VALUE;

public class OrderSummeryController {

    @FXML
    private TableView<Map<String, String>> tableView; // TableView for summary display
    @FXML
    private DatePicker datePicker; // DatePicker for selecting a date
    @FXML
    private ComboBox<String> optionComboBox; // ComboBox for selecting the option (1, 2, or 3)
    @FXML
    private Pagination pagination; // Pagination control

    private OrderRepository orderRepository = new OrderRepository(); // Order repository
    private List<Map<String, String>> allData = new ArrayList<>(); // Store all the data

    private static final int PAGE_SIZE = 13; // Number of rows per page


    /**
     * Load all orders placed on the given date into the TableView.
     *
     * @param date   Selected date to filter orders.
     * @param option Selected option to filter orders (1, 2, or 3).
     */
    public void loadOrdersByDate(LocalDate date, String option) {
        tableView.getColumns().clear(); // Clear existing columns
        tableView.getItems().clear();   // Clear existing data

        // Fetch order details filtered by both date and option
        Map<String, Map<String, Double>> orderDetails = orderRepository.getOrderDetailsByDateAndOption(date, option);

        if (orderDetails.isEmpty()) {
            System.out.println("No orders found for the selected date and option.");
            return;
        }
        // Get all product names (no limit)
        List<String> productNames = getProductNames(orderDetails);
        // Initialize product sums map
        Map<String, Double> productSums = initializeProductSums(productNames);
        // Add table columns
        addBranchColumn();
        addProductColumns(productNames);
        // Rotate column headers
        rotateColumnHeaders();
        // Populate table data and calculate product sums
        populateTableData(orderDetails, productNames, productSums);
        // Add summary row at the top
        addSummaryRow(productNames, productSums);
        // Initialize pagination
        initializePagination();
        addUpdateButtonColumn(productNames);
        addOrderIdColumn();

    }


    @FXML
    private void onFilterButtonClick() {
        LocalDate selectedDate = datePicker.getValue();
        String selectedOption = optionComboBox.getValue();

        if (selectedDate != null && selectedOption != null) {
            allData.clear(); // Clear any previous data to prevent duplication
            tableView.getItems().clear(); // Clear the TableView before loading new data
            loadOrdersByDate(selectedDate, selectedOption); // Load data based on both date and option
        } else {
            System.out.println("Please select both a date and an option.");
        }
    }

    private void addOrderIdColumn() {
        TableColumn<Map<String, String>, String> orderIdColumn = new TableColumn<>("Order ID");
        orderIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("orderId")));
        tableView.getColumns().add(0, orderIdColumn); // Add at the beginning
    }


    private void addUpdateButtonColumn(List<String> productNames) {
        TableColumn<Map<String, String>, Void> updateColumn = new TableColumn<>("Update");

        // Add a cell factory for rendering buttons in each row
        updateColumn.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button("Update");

            {
                // Add a click event to the button
                updateButton.setOnAction(event -> {
                    Map<String, String> rowData = getTableView().getItems().get(getIndex());

                    // Show the dialog with all products and quantities
                    showUpdateDialog(rowData, productNames);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : updateButton);
            }
        });

        tableView.getColumns().add(updateColumn);
    }

    private void showUpdateDialog(Map<String, String> rowData, List<String> productNames) {
        // Create a GridPane to hold the TextFields for each product
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        // Map to hold product quantity fields
        Map<String, TextField> productFields = new HashMap<>();

        // Populate GridPane with labels and fields
        for (int i = 0; i < productNames.size(); i++) {
            String productName = productNames.get(i);

            Label productLabel = new Label(productName + ":");
            TextField quantityField = new TextField(rowData.getOrDefault(productName, "0"));

            grid.add(productLabel, 0, i);
            grid.add(quantityField, 1, i);
            productFields.put(productName, quantityField);
        }

        // Create and show the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Product Quantities");
        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Handle user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                updateQuantities(rowData, productNames, productFields);
            }
        });
    }

    private void updateQuantities(Map<String, String> rowData, List<String> productNames, Map<String, TextField> productFields) {
        try {

            int orderId = 1;

            for (String productName : productNames) {
                String quantityText = productFields.get(productName).getText().trim();

                // Validate numeric input
                if (!quantityText.matches("\\d+")) {

                    showErrorDialog("Invalid quantity for " + productName + ". Please enter a valid number.");
                    return; // Stop updating if invalid input is found
                }

                int newQuantity = Integer.parseInt(quantityText);

                System.out.print(newQuantity);
                // Retrieve productId and update if valid
                String productIdKey = productName + "Id";
                if (rowData.containsKey(productIdKey)) {

                    int productId = Integer.parseInt(rowData.get(productIdKey));
                    orderRepository.updateOrderProductQuantity(orderId, productId, newQuantity);

                    rowData.put(productName, formatQuantity((double) newQuantity));

                }
            }

            System.out.println("Updated rowData: " + rowData);
            tableView.refresh(); // Refresh the table to show updated quantities
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            showErrorDialog("An error occurred while updating the order. Please try again.");
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Update Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private List<String> getProductNames(Map<String, Map<String, Double>> orderDetails)
    {
        if (!orderDetails.isEmpty()) {
            Map.Entry<String, Map<String, Double>> firstEntry = orderDetails.entrySet().iterator().next();
            return new ArrayList<>(firstEntry.getValue().keySet());
        }
        return new ArrayList<>();
    }

    // 4. Add Product columns
    private void addProductColumns(List<String> productNames) {
        // Loop through the product names in the order they appear in the database
        for (String productName : productNames) {
            TableColumn<Map<String, String>, String> productColumn = new TableColumn<>(productName);
            productColumn.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().getOrDefault(productName, "0")
            ));

            tableView.getColumns().add(productColumn);
        }
    }

    // 2. Initialize product sums map
    private Map<String, Double> initializeProductSums(List<String> productNames) {
        Map<String, Double> productSums = new HashMap<>();
        for (String productName : productNames) {
            productSums.put(productName, 0.0);
        }
        System.out.println(productNames);
        return productSums;
    }

    // 3. Add Branch column
    private void addBranchColumn() {
        TableColumn<Map<String, String>, String> branchColumn = new TableColumn<>("Branch");
        branchColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Branch")));
        tableView.getColumns().add(branchColumn);
    }

    // 6. Populate table data and calculate product sums
    private void populateTableData(Map<String, Map<String, Double>> orderDetails, List<String> productNames, Map<String, Double> productSums) {
        for (Map.Entry<String, Map<String, Double>> entry : orderDetails.entrySet()) {
            String branchName = entry.getKey();
            Map<String, Double> productQuantities = entry.getValue();

            Map<String, String> row = new HashMap<>();
            row.put("Branch", branchName);

            // Ensure you retrieve and add the orderId if available
            String orderId = productQuantities.containsKey("id") ? String.valueOf(productQuantities.get("id")) : "N/A";
            row.put("id", orderId);

            for (String productName : productNames) {
                Double quantity = productQuantities.containsKey(productName)
                        ? Double.parseDouble(productQuantities.get(productName).toString())
                        : 0.0;
                row.put(productName, formatQuantity(quantity));
            }
            allData.add(row);
        }
    }

    // 7. Add summary row
    private void addSummaryRow(List<String> productNames, Map<String, Double> productSums) {
        Map<String, String> summaryRow = new HashMap<>();
        summaryRow.put("Branch", "Total");

        for (int i = 0; i < productNames.size(); i++) {

            String productName = productNames.get(i);
            double productSum = productSums.getOrDefault(productName, 0.0);
            summaryRow.put(productName, formatQuantity(productSum));
        }
        allData.add(0, summaryRow);
    }

    // Format quantity
    private String formatQuantity(Double quantity) {
        if (quantity == quantity.intValue()) {
            return String.valueOf(quantity.intValue()); // If it's a whole number, return as an integer
        } else {
            return String.format("%.1f", quantity); // Otherwise, return as a decimal
        }
    }

    // 8. Initialize pagination
    private void initializePagination() {
        pagination.setPageCount((int) Math.ceil((double) allData.size() / PAGE_SIZE));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(pageIndex -> createTablePage(pageIndex));
    }

    private void rotateColumnHeaders() {
        // Loop through all the columns
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            // Skip the "Branch" column (or any other column you want to exclude from rotation)
            if ("Branch".equals(column.getText())) {
                continue;
            }
            tableView.widthProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    TableHeaderRow headerRow = (TableHeaderRow) tableView.lookup("TableHeaderRow");
                    if (headerRow != null) {
                        headerRow.setPrefHeight(120); // Set header row height

                    }
                });
            });

            Label rotatedLabel = new Label(column.getText());
            rotatedLabel.setRotate(-90); // Rotate 90 degrees counterclockwise for vertical text
            rotatedLabel.setPadding(new Insets(0, 0, 0, -45));
            rotatedLabel.setStyle("-fx-font-weight: normal; -fx-font-size: 12px; -fx-text-fill: black;");

            rotatedLabel.setWrapText(true); // Disable text wrapping
            rotatedLabel.setMaxWidth(MAX_VALUE); // Ensure label stretches
            rotatedLabel.setMaxHeight(MAX_VALUE); // Ensure the label stretches vertically

            // Set the rotated label as the column's graphic
            column.setGraphic(rotatedLabel);
            column.setText(""); // Remove the default column text since we are using the custom graphic

            // Adjust column width to fit the rotated text
            column.setPrefWidth(35); // Adjust the width as needed, or set it dynamically

        }
    }

    private VBox createTablePage(int pageIndex) {
        int startIndex = pageIndex * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, allData.size());

        // Clear the table before adding new rows
        tableView.getItems().clear();

        // Add the data for the current page
        for (int i = startIndex; i < endIndex; i++) {
            tableView.getItems().add(allData.get(i));
        }

        // Include the header row for clarity in printing
        VBox container = new VBox();
        container.getChildren().add(tableView);

        return container; // Return the VBox containing the TableView
    }

    // Method to find a printer by its name
    private Printer getPrinterByName(String printerName) {
        for (Printer printer : Printer.getAllPrinters()) {
            if (printer.getName().equalsIgnoreCase(printerName)) {
                return printer;
            }
        }
        return null; // Return null if printer not found
    }

}
