package com.example.foodordersystem.controller;

import com.example.foodordersystem.repository.OrderRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.*;
import java.time.LocalDate;
import java.util.*;



public class BillGenerateController {
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

    private static final int PAGE_SIZE = 14; // Number of rows per page


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

        // Fetch order details including branch, user, and products
        Map<String, Map<String, Map<String, Double>>> orderDetails = orderRepository.getOrderDetailsByDateAndOptionWithUser(date, option);

        if (orderDetails.isEmpty()) {
            System.out.println("No orders found for the selected date and option.");
            return;
        }

        // Get all product names
        List<String> productNames = getProductNames(orderDetails);

        // Initialize product sums map
        Map<String, Double> productSums = initializeProductSums(productNames);

        // Add columns
        addBranchColumn();
        addProductColumns(productNames);

        // Rotate column headers
        rotateColumnHeaders();

        // Populate table data and calculate product sums
        populateTableData(orderDetails, productNames, productSums);

        // Add summary row
        addSummaryRow(productNames, productSums);

        // Initialize pagination
        initializePagination();

        addPrintButtonColumn();
        addPrintButtonColumn3();
    }

//    public void loadOrdersByDate(LocalDate date, String option) {
//        tableView.getColumns().clear(); // Clear existing columns
//        tableView.getItems().clear();   // Clear existing data
//
//        // Fetch order details filtered by both date and option
//        Map<String, Map<String, Map<String, Double>>> orderDetails =
//                orderRepository.getOrderDetailsByDateAndOptionWithUser(date, option);
//
//        if (orderDetails.isEmpty()) {
//            System.out.println("No orders found for the selected date and option.");
//            return;
//        }
//
//        // Get all product names (no limit)
//        List<String> productNames = getProductNames(orderDetails);
//
//        // Initialize product sums map
//        Map<String, Double> productSums = initializeProductSums(productNames);
//
//        // Add table columns
//        addBranchColumn();
//        addProductColumns(productNames);
//
//        // Rotate column headers
//        rotateColumnHeaders();
//
//        // Populate table data and calculate product sums
//        //populateTableData(orderDetails, productNames, productSums);
//
//        // Add summary row at the top
//        addSummaryRow(productNames, productSums);
//        addUserColumn();
//        // Initialize pagination
//        initializePagination();
//        populateTableData(orderDetails, productNames, productSums);
//        addPrintButtonColumn();
//        addPrintButtonColumn3();
//    }

    private List<String> getProductNames(Map<String, Map<String, Map<String, Double>>> orderDetails) {
        if (!orderDetails.isEmpty()) {
            Map.Entry<String, Map<String, Map<String, Double>>> firstBranchEntry = orderDetails.entrySet().iterator().next();

            Map.Entry<String, Map<String, Double>> firstUserEntry = firstBranchEntry.getValue().entrySet().iterator().next();

            return new ArrayList<>(firstUserEntry.getValue().keySet());
        }
        return new ArrayList<>();
    }

//    private void addUserColumn() {
//        TableColumn<Map<String, String>, String> userColumn = new TableColumn<>("User");
//        userColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("User")));
//        tableView.getColumns().add(userColumn);
//    }


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
            productSums.put(productName, 0.0); // Initialize with 0.0 (Double)
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


    private void populateTableData(Map<String, Map<String, Map<String, Double>>> orderDetails, List<String> productNames, Map<String, Double> productSums) {
        for (Map.Entry<String, Map<String, Map<String, Double>>> branchEntry : orderDetails.entrySet()) {
            String branchName = branchEntry.getKey();
            Map<String, Map<String, Double>> userOrders = branchEntry.getValue();

            for (Map.Entry<String, Map<String, Double>> userEntry : userOrders.entrySet()) {
                String userName = userEntry.getKey();
                Map<String, Double> productQuantities = userEntry.getValue();

                Map<String, String> row = new HashMap<>();
                row.put("Branch", branchName);
                row.put("User", userName);

                double first6Total = 0.0;

                for (String productName : productNames) {
                    Double quantity = productQuantities.getOrDefault(productName, 0.0);
                    String formattedQuantity = formatQuantity(quantity);

                    row.put(productName, formattedQuantity);

                    try {
                        int productId = Integer.parseInt(productName.replaceAll("[^0-9]", ""));
                        if (productId >= 1 && productId <= 6) {
                            first6Total += quantity;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore non-numeric product names
                    }

                    productSums.put(productName, productSums.getOrDefault(productName, 0.0) + quantity);
                }

                row.put("First6Total", formatQuantity(first6Total));
                allData.add(row);
            }
        }
    }


    // 7. Add summary row
    private void addSummaryRow(List<String> productNames, Map<String, Double> productSums) {
        Map<String, String> summaryRow = new HashMap<>();
        summaryRow.put("Branch", "Total");

        double first6TotalSummary = 0.0;

        for (int i = 0; i < productNames.size(); i++) {
            String productName = productNames.get(i);
            double productSum = productSums.getOrDefault(productName, 0.0);

            summaryRow.put(productName, formatQuantity(productSum));

            if (i < 6) {
                first6TotalSummary += productSum;
            }
        }

        summaryRow.put("First6Total", formatQuantity(first6TotalSummary));
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
            rotatedLabel.setMaxWidth(Double.MAX_VALUE); // Ensure label stretches
            rotatedLabel.setMaxHeight(Double.MAX_VALUE); // Ensure the label stretches vertically


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

    private static final String BILL_NUMBER_FILE = "bill_number1.txt";

    private void addPrintButtonColumn() {
        TableColumn<Map<String, String>, Void> printColumn = new TableColumn<>("Print");

        printColumn.setCellFactory(param -> new TableCell<>() {
            private final Button printButton = new Button("Print");

            {
                printButton.setOnAction(event -> {
                    // Read the last bill number and increment
                    int billNumber = readLastBillNumber();
                    String billNumberStr = "BILL-" + billNumber;

                    // Save the new bill number
                    saveBillNumber(billNumber + 1);

                    // Get row data
                    Map<String, String> rowData = getTableView().getItems().get(getIndex());

                    // Get the selected date
                    LocalDate selectedDate = datePicker.getValue(); // Assuming you have a DatePicker

                    // Reorder row data (if needed)
                    List<Map.Entry<String, String>> reorderedEntries = reorderRowData(rowData);

                    // Generate HTML summary with the bill number
                    String htmlContent = generateHtmlSummary(reorderedEntries, selectedDate, rowData, billNumberStr);

                    // Show and print the HTML content
                    showAndPrintHtml(htmlContent);
                });
            }
            private int readLastBillNumber() {
                try {
                    File file = new File(BILL_NUMBER_FILE);
                    if (file.exists()) {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        int lastNumber = Integer.parseInt(reader.readLine());
                        reader.close();
                        return lastNumber;
                    }
                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace();
                }
                return 1; // Default to 1 if no file exists
            }
            private void saveBillNumber(int billNumber) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(BILL_NUMBER_FILE));
                    writer.write(String.valueOf(billNumber));
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(printButton);
                }
            }
        });

        tableView.getColumns().add(printColumn);
    }



    private static final String BILL_NUMBER_FILE1 = "bill_number2.txt";

    private void addPrintButtonColumn3() {
        TableColumn<Map<String, String>, Void> printColumn = new TableColumn<>("Print");

        printColumn.setCellFactory(param -> new TableCell<>() {
            private final Button printButton = new Button("Print");

            {
                printButton.setOnAction(event -> {
                    // Read the last bill number and increment
                    int billNumber = readLastBillNumber1();
                    String billNumberStr = "BILL-" + billNumber;

                    // Save the new bill number
                    saveBillNumber1(billNumber + 1);

                    // Get row data
                    Map<String, String> rowData = getTableView().getItems().get(getIndex());

                    // Get the selected date
                    LocalDate selectedDate = datePicker.getValue(); // Assuming you have a DatePicker

                    // Reorder row data (if needed)
                    List<Map.Entry<String, String>> reorderedEntries = reorderRowData(rowData);

                    // Generate HTML summary with the bill number
                    String htmlContent = generateHtmlSummary2(reorderedEntries, selectedDate, rowData, billNumberStr);

                    // Show and print the HTML content
                    showAndPrintHtml(htmlContent);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(printButton);
                }
            }
        });

        tableView.getColumns().add(printColumn);
    }
    private int readLastBillNumber1() {
        try {
            File file = new File(BILL_NUMBER_FILE1);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                int lastNumber = Integer.parseInt(reader.readLine());
                reader.close();
                return lastNumber;
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 1; // Default to 1 if no file exists
    }

    private void saveBillNumber1(int billNumber) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(BILL_NUMBER_FILE1));
            writer.write(String.valueOf(billNumber));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Map.Entry<String, String>> reorderRowData(Map<String, String> rowData) {
        List<Map.Entry<String, String>> entries = new ArrayList<>(rowData.entrySet());

        // Define the new order of indices. For example, move item at index 2 to the first position, etc.
        int[] newOrder = {11,4,6,0,23,22,1,14,7,5,21,9,2,17,18,24,15,16,8,19,10,13};


        List<Map.Entry<String, String>> reorderedEntries = new ArrayList<>();
        for (int index : newOrder) {
            if (index < entries.size()) {
                Map.Entry<String, String> entry = entries.get(index);
                reorderedEntries.add(entry);

//                // Print the product name and its index
               // System.out.println("Index: " + index + " | Product: " + entry.getKey());
            }
        }

        return reorderedEntries;
    }

    private String generateHtmlSummary(List<Map.Entry<String, String>> reorderedEntries, LocalDate selectedDate,
                                       Map<String, String> rowData, String billNumber) {
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<html>")
                .append("<head>")
                .append("<title>Order Summary</title>")
                .append("<style>")
                .append("@page { size: auto; margin: 10px; }")
                .append("table { width: 85%; border-collapse: collapse; margin-top: 3px; style='border: 1px solid black;' }")
                .append("th, td { border: 0.3px solid black ;font-size: 10px; padding: 2.5px;}")
                .append("th { background-color:#050505; }")
                .append("p {font-size: 12px;}")
                .append("</style>")
                .append("</head>")
                .append("<p><strong>Date:</strong> ").append(selectedDate).append("   | Branch: ").append(rowData.get("Branch")).append("<strong> &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp Bill No:</strong> ").append(billNumber).append("</p>")
                .append("<table>")
                .append("<tr>")
                .append("<th>Product</th>")
                .append("<th>Quantity</th>")
                .append("<th>AAAAAAAAAAA</th>") // Added Price column header
                .append("<th>basAAAA</th>") // Added 'bas' column header
                .append("<th>aaa</th>")
                .append("</tr>");


        // Add existing products from rowData
        int rowIndex = 1; // Initialize row index for the fourth column (4th row)


        for (Map.Entry<String, String> entry : reorderedEntries) {
            if (!"Branch".equals(entry.getKey()) && !"First6Total".equals(entry.getKey())) {
                htmlBuilder.append("<tr>")
                        .append("<td>").append(entry.getKey()).append("</td>")
                        .append("<td>").append(entry.getValue());

                int quantity = 0;
                try {
                    quantity = Integer.parseInt(entry.getValue());
                } catch (NumberFormatException e) {
                    // If parsing fails, keep quantity as 0
                }

                // Append 'KG' for index 14 if value is not 0
                if (rowIndex == 8 && quantity != 0) {
                    htmlBuilder.append(" Kg ");
                }

                // Append 'L' for index 20 if value is not 0
                if (rowIndex == 9 && quantity != 0) {
                    htmlBuilder.append(" ( වළලු )");
                }

                htmlBuilder.append("</td>")
                        .append("<td></td>");


                // Add the value for the new 'bas' column in the 4th column, 4th row
                if (rowIndex == 1) {
                    htmlBuilder.append("<td>1100/=</td>")
                     .append("<td></td>");
                }
                if (rowIndex == 2) {
                    htmlBuilder.append("<td>1200/=</td>")
                     .append("<td></td>");
                }
                if (rowIndex == 3) {
                    htmlBuilder.append("<td>1400/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 4) {
                    htmlBuilder.append("<td>1500/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 5) {
                    htmlBuilder.append("<td>1600/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 6) {
                    htmlBuilder.append("<td>1700/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 7) {
                    htmlBuilder.append("<td>2000/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 8) {
                    htmlBuilder.append("<td>2500/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 9) {
                    htmlBuilder.append("<td>2750/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 10) {
                    htmlBuilder.append("<td>3250/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 11) {
                    htmlBuilder.append("<td>1000/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 12) {
                    htmlBuilder.append("<td>900/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 13) {
                    htmlBuilder.append("<td>800/=</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 14) {
                    htmlBuilder.append("<td>W/C/S</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 15) {
                    htmlBuilder.append("<td>150/=C/I</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 16) {
                    htmlBuilder.append("<td>J/Cup</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 17) {
                    htmlBuilder.append("<td>B/Cup</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 18) {
                    htmlBuilder.append("<td>A/Cup</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 19) {
                    htmlBuilder.append("<td>G.කප් කේක්</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 20) {
                    htmlBuilder.append("<td>කප් කේක්</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 21) {
                    htmlBuilder.append("<td>T/Box</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 22) {
                    htmlBuilder.append("<td>U/Log</td>")
                            .append("<td></td>");
                }
                if (rowIndex == 23) {
                    htmlBuilder.append("<td>Mi/Tub</td>")
                            .append("<td></td>");
                }

                htmlBuilder.append("</tr>");
                rowIndex++;
            }

        }
        htmlBuilder.append("<tr>")

                .append("<td style='border: 1px solid black;height: 18px;'>ලො කු පා න්</td>")
                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'>Mi/Tub</td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")


                .append("</tr>");

        String[] products = {"තැ ටි පා න්", "රෝ ස් පා න්", "වියන් රෝ ල්", "මා ළු පා න්", "සීනි සම්බල් පා න්", "අච්චු පා න්", "සීනි බනිස්", "පො ඩි පා න්", "පේස්ට්‍රි", "ජෑ ම් පා න්", "මා ළු කෑ ම", "බිත්තර කෑ ම"};

        // Add products and prices to the table
        for (int i = 0; i < products.length; i++) {
            htmlBuilder.append("<tr>")
                    .append("<td>").append(products[i]).append("</td>")
                    .append("<td></td>")
                    .append("<td></td>")
                    .append("<td></td>")
                    .append("<td></td>")
                    .append("</tr>");
        }

        htmlBuilder.append("</table>");

        htmlBuilder.append("<p style='font-size: 12px;'><strong>Checked By: ____________________-</strong> ").append("<strong> &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp User Name:</strong> ").append(rowData.get("User"))
                .append("</p>");

                htmlBuilder.append("</body>")
                .append("</html>");

        return htmlBuilder.toString();
    }

    private void showAndPrintHtml(String htmlContent) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Load the HTML content
        webEngine.loadContent(htmlContent);

        // Set up a listener to handle when the page is fully loaded
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Create the printer job
                PrinterJob printerJob = PrinterJob.createPrinterJob();
                if (printerJob != null) {
                    // Show the print dialog box to the user
                    if (printerJob.showPrintDialog(null)) {
                        // Set full-page printing (A4, PORTRAIT, with zero margins)
                        Printer printer = printerJob.getPrinter();
                        PageLayout pageLayout = printer.createPageLayout(
                                Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM
                        );

                        // Ensure WebView content fits the full page
                        webView.setPrefWidth(pageLayout.getPrintableWidth());
                        webView.setPrefHeight(pageLayout.getPrintableHeight());

                        // Print the WebView content
                        boolean printed = printerJob.printPage(webView);
                        if (printed) {
                            printerJob.endJob();
                            System.out.println("Printed successfully.");
                        } else {
                            System.out.println("Printing failed.");
                        }
                    } else {
                        System.out.println("Print dialog was cancelled.");
                    }
                } else {
                    System.out.println("Failed to create printer job.");
                }
            }
        });
    }


    private double calculateTotalQuantity(List<Map.Entry<String, String>> reorderedEntries) {
        // Indices of products to include in the total quantity
        int[] relevantIndices = {0,1,2,3,4,5,6};
        double totalQuantity = 0;

        // Iterate through relevant indices and add quantities
        for (int index : relevantIndices) {

            if (index < reorderedEntries.size()) {
                try {
                    // Parse the quantity value as an integer
                    totalQuantity += Double.parseDouble(reorderedEntries.get(index).getValue());
                    System.out.println(totalQuantity);
                } catch (NumberFormatException e) {
                    // Handle cases where the value is not a valid number
                    System.err.println("Invalid quantity at index " + index + ": " + reorderedEntries.get(index).getValue());
                }
            }
        }

        System.out.println(totalQuantity);

        return totalQuantity;
    }


    private String generateHtmlSummary2(List<Map.Entry<String, String>> reorderedEntries1, LocalDate selectedDate,
                                       Map<String, String> rowData, String billNumber) {
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<html>")
                .append("<head>")
                .append("<title>Order Summary</title>")
                .append("<style>")
                .append("@page { size: auto; margin: 5px; }")
                .append("table { width: 85%; border-collapse: collapse; margin-top: 3px; style='border: 1px solid black;'}")
                .append("th, td { border: 0.5px solid black;font-size: 10px; padding: 3px;}")
                .append("th { background-color:#050505; }")
                .append("p {font-size: 12px;}")
                .append("</style>")
                .append("</head>")
                .append("<p><strong>Date:</strong> ").append(selectedDate).append("   |<strong> Branch: </strong>").append(rowData.get("Branch")).append("<strong> &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp Bill No:</strong> ").append(billNumber).append("</p>")
                .append("<table>")
                .append("<tr>")
                .append("<th>Product</th>")
                .append("<th>Quantity</th>")
                .append("<th></th>")
                .append("<th>AAAA</th>") // Added Price column header
                .append("<th></th>") // Added 'bas' column header
                .append("<th>aaaa</th>")
                .append("</tr>");


        // Add existing products from rowData
        int rowIndex = 1; // Initialize row index for the fourth column (4th row)

        for (Map.Entry<String, String> entry : reorderedEntries1) {
            if (!"Branch".equals(entry.getKey()) && !"First6Total".equals(entry.getKey())) {
                if (rowIndex == 20) {
                    rowIndex++;
                    continue;
                }
                htmlBuilder.append("<tr>")
                        .append("<td>").append(entry.getKey()).append("</td>")
                        .append("<td>").append(entry.getValue());

                int quantity = 0;
                try {
                    quantity = Integer.parseInt(entry.getValue());
                } catch (NumberFormatException e) {
                    // If parsing fails, keep quantity as 0
                }

                // Append 'KG' for index 14 if value is not 0
                if (rowIndex == 8 && quantity != 0) {
                    htmlBuilder.append(" Kg ");
                }

                // Append 'L' for index 20 if value is not 0
                if (rowIndex == 9 && quantity != 0) {
                    htmlBuilder.append(" ( වළලු ) ");
                }

                htmlBuilder.append("</td>");


                if (rowIndex == 1) {
                    htmlBuilder.append("<td colspan='2'></td>");
                }
                if (rowIndex == 2) {
                    htmlBuilder.append("<td colspan='2'></td>");
                }
                if (rowIndex == 3) {
                    htmlBuilder.append("<td colspan='2'></td>");
                }
                if (rowIndex == 4) {
                    htmlBuilder.append("<td colspan='2'> </td>");
                }
                if (rowIndex == 5) {
                    htmlBuilder.append("<td colspan='2'></td>");
                }
                if (rowIndex == 6) {
                    htmlBuilder.append("<td colspan='2'></td>");
                }
                if (rowIndex == 7) {
                    htmlBuilder.append("<td colspan='2'></td>");
                }
                if (rowIndex == 8) {
                    htmlBuilder.append("<td>CI Kg</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>2000/=</td>");
                }
                if (rowIndex == 9) {
                    htmlBuilder.append("<td>BI Kg</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>2100/=</td>");
                }
                if (rowIndex == 10) {
                    htmlBuilder.append("<td>RI Kg</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>2500/=</td>");
                }
                if (rowIndex == 11) {
                    htmlBuilder.append("<td>CI කෑ ලි</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>2600/=</td>");
                }
                if (rowIndex == 12) {
                    htmlBuilder.append("<td>BI කෑ ලි</td>");
                          htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>2750/=</td>");
                }
                if (rowIndex == 13) {
                    htmlBuilder.append("<td>RI කෑ ලි</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>3000/=</td>");
                }
                if (rowIndex == 14) {
                    htmlBuilder.append("<td> ∆ කෑ ලි</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>3100/=</td>");
                }
                if (rowIndex == 15) {
                    htmlBuilder.append("<td>W/C/S</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>3250/=</td>");
                }
                if (rowIndex == 16) {
                    htmlBuilder.append("<td>G/Cup</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>3500/=</td>");
                }
                if (rowIndex == 17) {
                    htmlBuilder.append("<td>N/I කෑ ලි </td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>3750/=</td>");
                }
                if (rowIndex == 18) {
                    htmlBuilder.append("<td>B/Cup </td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>4000/=</td>");
                }
                if (rowIndex == 19) {
                    htmlBuilder.append("<td>J/Cup </td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>5000/=</td>");
                }
                if (rowIndex == 21) {
                    htmlBuilder.append("<td>U/Log</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>6000/=</td>");

                }
                if (rowIndex == 22) {
                    htmlBuilder.append("<td>A/Cup</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>7000/=</td>");
                }
                if (rowIndex == 23) {
                    htmlBuilder.append("<td>T/Box</td>");
                    htmlBuilder.append("<td></td>");
                    htmlBuilder.append("<td>8000/=</td>");
                }

                // Add the value for the new 'bas' column in the 4th column, 4th row
                if (rowIndex == 1) {
                    htmlBuilder.append("<td>1100/=</td>");
                }
                if (rowIndex == 2) {
                    htmlBuilder.append("<td>1200/=</td>");
                }
                if (rowIndex == 3) {
                    htmlBuilder.append("<td>1400/=</td>");
                }
                if (rowIndex == 4) {
                    htmlBuilder.append("<td>1500/=</td>");
                }
                if (rowIndex == 5) {
                    htmlBuilder.append("<td>1600/=</td>");
                }
                if (rowIndex == 6) {
                    htmlBuilder.append("<td>1700/=</td>");
                }
                if (rowIndex == 7) {
                    htmlBuilder.append("<td>1750/=</td>");
                }
//                if (rowIndex == 8) {
//                    htmlBuilder.append("<td>2000/=</td>");
//                }
//                if (rowIndex == 9) {
//                    htmlBuilder.append("<td>2500/=</td>");
//                }
//                if (rowIndex == 10) {
//                    htmlBuilder.append("<td>2600/=</td>");
//                }
//                if (rowIndex == 11) {
//                    htmlBuilder.append("<td>2750/=</td>");
//                }
//                if (rowIndex == 12) {
//                    htmlBuilder.append("<td>3000/=</td>");
//                }
//                if (rowIndex == 13) {
//                    htmlBuilder.append("<td>3100/=</td>");
//                }
//                if (rowIndex == 14) {
//                    htmlBuilder.append("<td>3250/=</td>");
//                }
//                if (rowIndex == 15) {
//                    htmlBuilder.append("<td>3750/=</td>");
//                }
//                if (rowIndex == 16) {
//                    htmlBuilder.append("<td>4000/=<td>");
//                }
//                if (rowIndex == 17) {
//                    htmlBuilder.append("<td>5000/=</td>");
//                }
//                if (rowIndex == 18) {
//                    htmlBuilder.append("<td>6000/=</td>");
//                }
//                if (rowIndex == 19) {
//                    htmlBuilder.append("<td>7000/=</td>");
//                }
//                if (rowIndex == 20) {
//                    htmlBuilder.append("<td>8000/=</td>");
//                }
//                if (rowIndex == 21) {
//                    htmlBuilder.append("<td></td>");
//                }
//                if (rowIndex == 22) {
//                    htmlBuilder.append("<td></td>");
//                }
//                if (rowIndex == 23) {
//                    htmlBuilder.append("<td></td>");
//                }
//                if (rowIndex == 24) {
//                    htmlBuilder.append("<td></td>");
//                }

                htmlBuilder.append("<td></td>");

                htmlBuilder.append("</tr>");
                rowIndex++;

            }

        }

        htmlBuilder.append("<tr>")

                .append("<td style='border: 1px solid black;height: 18px;'>කප් කේක්</td>")
                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td style='border: 1px solid black; height: 18px;'>T/Box</td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'>8000/</td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")


                .append("</tr>");
        htmlBuilder.append("<tr>")

                .append("<td style='border: 1px solid black;height: 18px;'></td>")
                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td style='border: 1px solid black; height: 18px;'>P/Cup</td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")


                .append("</tr>");
        htmlBuilder.append("<tr>")

                .append("<td style='border: 1px solid black;height: 18px;'></td>")
                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td rowspan='3' colspan='2' style='border: 1px solid black; height: 18px;;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")


                .append("</tr>");

        htmlBuilder.append("<tr>")

                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("</tr>");

        htmlBuilder.append("<tr>")

                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("<td style='border: 1px solid black;  height: 18px;'></td>")
                .append("</tr>");

        htmlBuilder.append("<tr>")

                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td colspan='4' style='border: 1px solid black;'></td>")

                .append("</tr>");

        htmlBuilder.append("<tr>")

                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td style='border: 1px solid black;height: 18px;'></td>")
                .append("<td colspan='4' style='border: 1px solid black;'></td>")

                .append("</tr>");

        htmlBuilder.append("<tr>")

                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td style='border: 1px solid black; height: 18px;'></td>")
                .append("<td colspan='4' rowspan='6' style='border: 1px solid black; '></td>")

                .append("</tr>");

        htmlBuilder.append("<tr>")
//                .append("<td colspan='3' style='border: 1px solid black; text-align: center;'>This cell spans 3 columns</td>")
                .append("<td colspan='2' rowspan='3' style='border:1px solid black; height: 58px;'></td>")

                .append("</tr>");

        htmlBuilder.append("</table>");
        htmlBuilder.append("</table>");
        htmlBuilder .append("</body>");
        htmlBuilder.append("<p style='font-size: 12px;'><strong>Checked By: _ __ _ _ _ _ _ _ _ _ _ _ _ _ _-</strong> ")
                .append("<strong> &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp User Name:</strong> ").append(rowData.get("User"))
                .append("<strong> &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp තැ ටි ගණන: _ _ _ _ _ _ _ _ _</strong> ")
                .append("</p>");
        htmlBuilder.append("</html>");

        return htmlBuilder.toString();
    }


}
