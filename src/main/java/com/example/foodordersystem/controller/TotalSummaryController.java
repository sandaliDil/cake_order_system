package com.example.foodordersystem.controller;

import com.example.foodordersystem.repository.OrderRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.*;
import java.util.List;

import static java.lang.Double.*;

public class TotalSummaryController {

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

    private static final int PAGE_SIZE = 15; // Number of rows per page


    @FXML
    public void initialize() {
        optionComboBox.getItems().addAll( "Combined (Option 1 + 2)");
    }

    @FXML
    private void onFilterButtonClick() {

        LocalDate selectedDate = datePicker.getValue();
        String selectedOption = optionComboBox.getValue();

        if (selectedDate != null && selectedOption != null) {
            allData.clear();
            tableView.getItems().clear();

            if (selectedOption.equals("Combined (Option 1 + 2)")) {
                loadOrdersByDateCombined(selectedDate);
            } else {
                loadOrdersByDate(selectedDate, selectedOption);
            }
        } else {
            System.out.println("Please select both a date and an option.");
        }
    }

    private void addDeleteButtonColumn() {
        TableColumn<Map<String, String>, Void> deleteColumn = new TableColumn<>("Delete");

        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("X");

            {
                deleteButton.setOnAction(event -> {
                    Map<String, String> rowData = getTableView().getItems().get(getIndex());
                    removeRow(rowData);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        tableView.getColumns().add(deleteColumn);
    }

    private void removeRow(Map<String, String> rowData) {
        if (rowData != null) {
            allData.remove(rowData);
            tableView.getItems().remove(rowData);
            updateProductTotals();
        }
    }

    private void updateProductTotals() {
        Map<String, Double> productSums = new HashMap<>();

        // Iterate through all rows and recalculate totals
        for (Map<String, String> row : allData) {
            if (!"Total".equals(row.get("Branch"))) { // Ignore the "Total" row in calculation
                double rowSum = 0.0;
                int count = 0;

                for (String key : row.keySet()) {
                    if (!key.equals("Branch") && !key.equals("First6Total")) {
                        try {
                            double value = Double.parseDouble(row.get(key));
                            productSums.put(key, productSums.getOrDefault(key, 0.0) + value);

                            // Sum up the first six product values for this row
                            if (count < 6) {
                                rowSum += value;
                                count++;
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid values
                        }
                    }
                }

                // Update "First6Total" for the row
                row.put("First6Total", formatQuantity(rowSum));
            }
        }

        // Update the "Total" row with the recalculated values
        for (Map<String, String> row : allData) {
            if ("Total".equals(row.get("Branch"))) {
                double totalFirst6Total = 0.0;
                for (String key : productSums.keySet()) {
                    row.put(key, formatQuantity(productSums.get(key))); // Ensure correct formatting

                    // Sum up "First6Total" column for the total row
                    if (!key.equals("First6Total")) {
                        totalFirst6Total += productSums.get(key);
                    }
                }
                row.put("First6Total", formatQuantity(totalFirst6Total));
                break;
            }
        }

        // Refresh UI to reflect changes
        tableView.refresh();
    }

    // Call this method after deleting a row
    private void deleteRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < allData.size()) {
            allData.remove(rowIndex);
            updateProductTotals(); // Recalculate totals after deletion
            tableView.refresh(); // Ensure UI reflects the changes
        }

    }

    public void loadOrdersByDateCombined(LocalDate date) {
        tableView.getColumns().clear(); // Clear existing columns
        tableView.getItems().clear();   // Clear existing data

        // Fetch data for both Option 1 and Option 2
        Map<String, Map<String, Double>> option1Data = orderRepository.getOrderDetailsByDateAndOption(date, "අපේ කඩ");
        Map<String, Map<String, Double>> option2Data = orderRepository.getOrderDetailsByDateAndOption(date, "ළග කඩ");

        // Merge both datasets
        Map<String, Map<String, Double>> mergedData = new HashMap<>();

        for (String branch : option1Data.keySet()) {
            mergedData.put(branch, new HashMap<>(option1Data.get(branch)));
        }

        for (String branch : option2Data.keySet()) {
            mergedData.putIfAbsent(branch, new HashMap<>());

            Map<String, Double> branchData = mergedData.get(branch);
            for (String product : option2Data.get(branch).keySet()) {
                branchData.put(product, branchData.getOrDefault(product, 0.0) +
                        option2Data.get(branch).get(product));
            }
        }

        if (mergedData.isEmpty()) {
            System.out.println("No orders found for the selected date and combined options.");
            return;
        }

        // Get all product names
        List<String> productNames = getProductNames(mergedData);
        System.out.println(productNames);
        // Initialize product sums map
        Map<String, Double> productSums = initializeProductSums(productNames);

        // Add table columns
        addBranchColumn();
        addProductColumns(productNames);
        rotateColumnHeaders();

        // Populate table data and calculate product sums
        populateTableData(mergedData, productNames, productSums);
        addFirst6TotalColumn();

        // Add summary row
        addSummaryRow(productNames, productSums);

        // Initialize pagination
        initializePagination();

        addDeleteButtonColumn();
    }
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
        addFirst6TotalColumn();

        // Add summary row at the top
        addSummaryRow(productNames, productSums);
        initializePagination();

//      addUpdateButtonColumn(productNames);

        addDeleteButtonColumn();
    }

    private List<String> getProductNames(Map<String, Map<String, Double>> orderData) {
        // Define the desired product order manually
        List<String> customOrder = Arrays.asList("පො ල්  කේක්", "බටර් කේක් ", "චො කලට් කේක් ", "ෆෘ ට් කේක්", "රිබන් කේක් ",
                "බටර් ලේයර් කේ ක් ","ප්ලේන්ටි කේක්", "දො දො ල් ", "පැ ණි වළලු", "ස්පන්චි", "ටී බනිස්","මැ කරො නි ", "සුල්තා නා ","අඩපංචි",
                "විශේෂ පා න් පො ඩි ", "විශේෂ පා න් ලො කු ", "රො  ක් කේ ක්", "රන් කේක්", "බනිස් ගෙඩි","චො කලට් රෝ ල් කැ ලි",
                "චො කලට් රෝ ල් 1250/=","ක්\u200Dරීම් බනිස් ");

        Set<String> availableProducts = new HashSet<>();
        for (Map<String, Double> branchData : orderData.values()) {
            availableProducts.addAll(branchData.keySet());
        }

        // Filter and retain only available products in the specified order
        List<String> productList = new ArrayList<>();
        for (String product : customOrder) {
            if (availableProducts.contains(product)) {
                productList.add(product);
            }
        }

        return productList;
    }

    private void addProductColumns(List<String> productNames) {
        for (String productName : productNames) {
            TableColumn<Map<String, String>, String> productColumn = new TableColumn<>(productName);
            productColumn.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().getOrDefault(productName, "0")
            ));
            tableView.getColumns().add(productColumn);
        }
    }

    private void addFirst6TotalColumn() {
        TableColumn<Map<String, String>, String> totalColumn = new TableColumn<>("");
        totalColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getOrDefault("First6Total", "0")
        ));
        tableView.getColumns().add(totalColumn);
    }

    private Map<String, Double> initializeProductSums(List<String> productNames) {
        Map<String, Double> productSums = new HashMap<>();
        for (String productName : productNames) {
            productSums.put(productName, 0.0);
        }
        productSums.put("First6Total", 0.0);
        return productSums;
    }

    private void populateTableData(Map<String, Map<String, Double>> orderDetails, List<String> productNames,
                                   Map<String, Double> productSums) {
        for (Map.Entry<String, Map<String, Double>> entry : orderDetails.entrySet()) {
            String branchName = entry.getKey();
            Map<String, Double> productQuantities = entry.getValue();

            Map<String, String> row = new HashMap<>();
            row.put("Branch", branchName);

            double first6Total = 0.0;
            for (int i = 0; i < 7; i++) {
                String productName = productNames.get(i);
                Double quantity = productQuantities.getOrDefault(productName, 0.0);
                row.put(productName, formatQuantity(quantity));

                if (i < 7) {
                    first6Total += quantity;
                }
                productSums.put(productName, productSums.getOrDefault(productName, 0.0) + quantity);
            }
            row.put("First6Total", formatQuantity(first6Total));
            productSums.put("First6Total", productSums.get("First6Total") + first6Total);
            allData.add(row);
        }
    }

    // 3. Add Branch column
    private void addBranchColumn() {
        TableColumn<Map<String, String>, String> branchColumn = new TableColumn<>("Branch");
        branchColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()
                .get("Branch")));
        tableView.getColumns().add(branchColumn);
    }

    private void addSummaryRow(List<String> productNames, Map<String, Double> productSums) {
        Map<String, String> summaryRow = new HashMap<>();
        summaryRow.put("Branch", "Total");

        for (String productName : productNames) {
            summaryRow.put(productName, formatQuantity(productSums.getOrDefault(productName, 0.0)));
        }
        summaryRow.put("First6Total", formatQuantity(productSums.get("First6Total")));
        allData.add(summaryRow);
    }

    private String formatQuantity(Double quantity) {
        return (quantity == quantity.intValue()) ? String.valueOf(quantity.intValue()) :
                String.format("%.2f", quantity);
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

            tableView.widthProperty().addListener((obs, oldVal, newVal)
                    -> {Platform.runLater(() -> {
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

        // Create a label to display the selected date
        LocalDate selectedDate = datePicker.getValue();
        Label dateLabel = new Label("Date: " + (selectedDate != null ? selectedDate.toString() : "Not selected"));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Include the header row for clarity in printing
        VBox container = new VBox();
        container.getChildren().addAll(dateLabel, tableView);

        tableView.setPrefWidth(1050);  // Adjust the width as needed
        tableView.setMinWidth(1050);   // Minimum width
        tableView.setMaxWidth(1050);   // Maximum width

        return container; // Return the VBox containing the date label and TableView
    }

    @FXML
    private void onPrintButtonClick() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();

        if (printerJob != null && printerJob.showPrintDialog(null)) { // Show print dialog
            Printer selectedPrinter = printerJob.getPrinter();

            if (selectedPrinter != null) {
                PageLayout pageLayout = selectedPrinter.createPageLayout(Paper.LEGAL,
                        PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM);

                double pageWidth = pageLayout.getPrintableWidth();
                double pageHeight = pageLayout.getPrintableHeight();

                int totalPages = pagination.getPageCount();

                for (int i = 0; i < totalPages; i++) {
                    VBox pageContent = createTablePage(i);
                    pageContent.setPrefSize(pageWidth, pageHeight);

                    boolean success = printerJob.printPage(pageLayout, pageContent);
                    if (!success) {
                        System.out.println("Failed to print page: " + (i + 1));
                        break;
                    }
                }
                printerJob.endJob();
            } else {
                System.out.println("No printer selected.");
            }
        } else {
            System.out.println("Print job cancelled.");
        }
    }

    public void onApplyColorClick(ActionEvent actionEvent) {
        tableView.refresh();
    }
}