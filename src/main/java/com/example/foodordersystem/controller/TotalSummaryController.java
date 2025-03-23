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
import javafx.scene.control.ColorPicker;

import java.awt.*;
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
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button applyColorButton;

    private OrderRepository orderRepository = new OrderRepository(); // Order repository
    private List<Map<String, String>> allData = new ArrayList<>(); // Store all the data

    private static final int PAGE_SIZE = 19; // Number of rows per page


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

        // Initialize pagination
        initializePagination();
//        addUpdateButtonColumn(productNames);
    }



    private List<String> getProductNames(Map<String, Map<String, Double>> orderDetails) {
        if (!orderDetails.isEmpty()) {
            Map.Entry<String, Map<String, Double>> firstEntry = orderDetails.entrySet().iterator().next();
            return new ArrayList<>(firstEntry.getValue().keySet());
        }
        return new ArrayList<>();
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
            for (int i = 0; i < productNames.size(); i++) {
                String productName = productNames.get(i);
                Double quantity = productQuantities.getOrDefault(productName, 0.0);
                row.put(productName, formatQuantity(quantity));


                if (i < 7) {
                    first6Total += quantity;
                    System.out.println(quantity);
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
                String.format("%.1f", quantity);
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
        dateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px;");

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


    // Method to find a printer by its name
    private Printer getPrinterByName(String printerName) {
        for (Printer printer : Printer.getAllPrinters()) {
            if (printer.getName().equalsIgnoreCase(printerName)) {
                return printer;
            }
        }
        return null; // Return null if printer not found
    }

    private List<Map.Entry<String, String>> reorderRowData(Map<String, String> rowData) {
        List<Map.Entry<String, String>> entries = new ArrayList<>(rowData.entrySet());

        // Define the new order of indices. Adjust based on your needs.
        int[] newOrder = {15, 5, 10, 0, 20, 19, 2, 21, 11, 6, 17, 13, 3, 4, 7, 22, 23, 12, 1, 8, 14, 18};

        // Create a new list with the reordered entries
        List<Map.Entry<String, String>> reorderedEntries = new ArrayList<>();
        for (int index : newOrder) {
            if (index < entries.size()) {
                reorderedEntries.add(entries.get(index));
            }
        }

        // Calculate the total for indices 0, 1, 2, 3, 4, 5, 6
        double totalQuantity = 0.0;
        for (int i = 0; i <= 6; i++) {
            if (i < entries.size()) {
                try {
                    // Parse the value as a double
                    totalQuantity += Double.parseDouble(entries.get(i).getValue());
                } catch (NumberFormatException e) {
                    // Ignore non-numeric values
                }
            }
        }

        // Add a new entry for the total quantity
        Map.Entry<String, String> totalEntry = new AbstractMap.SimpleEntry<>("TotalQuantity",
                String.valueOf(totalQuantity));
        reorderedEntries.add(totalEntry);
        System.out.println(totalQuantity);
        return reorderedEntries;
    }

    public void onApplyColorClick(ActionEvent actionEvent) {
        tableView.refresh();
    }
}