package com.example.foodordersystem.controller;

import com.example.foodordersystem.Session;
import com.example.foodordersystem.model.*;
import com.example.foodordersystem.repository.BranchRepository;
import com.example.foodordersystem.repository.OrderRepository;
import com.example.foodordersystem.repository.UserRepository;
import com.example.foodordersystem.service.BranchService;
import com.example.foodordersystem.service.OrderService;
import com.example.foodordersystem.service.ProductService;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.*;

import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class OrderDetailsController {

    @FXML
    private TableView<Product> productTable1;
    @FXML
    private TableView<Product> productTable2;
    @FXML
    private TableView<Product> productTable3;
    @FXML
    private TableColumn<Product, String> productNameColumn1;
    @FXML
    private TableColumn<Product, String> productNameColumn2;
    @FXML
    private TableColumn<Product, String> productNameColumn3;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label usernameText;
    @FXML
    private DatePicker orderDatePicker;
    @FXML
    private ComboBox<String> branchComboBox;
    @FXML
    private TableView<Product> tableView;
    @FXML
    private CheckBox checkbox1;
    @FXML
    private CheckBox checkbox2;
    @FXML
    private CheckBox checkbox3;
    @FXML
    private TextField orderIdTextField;
    @FXML
    private ComboBox<String> printerComboBox;
    @FXML
    private Label totalQuantityLabel;




    private final OrderRepository orderRepository = new OrderRepository();
    private final UserRepository userRepository = new UserRepository();
    private final BranchRepository branchRepository = new BranchRepository();
    private BranchService branchService;
    private ProductService productService;
    private OrderService orderService;
    private ObservableList<String> branchNames;

    private Map<Integer, TextField> productQuantityMap = new HashMap<>();

    public OrderDetailsController() {

        this.productService = new ProductService();
        this.branchService = new BranchService();
        this.orderService = new OrderService();
    }

    @FXML
    public void initialize() {
        productNameColumn1.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productNameColumn2.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productNameColumn3.setCellValueFactory(new PropertyValueFactory<>("productName"));

        if (tableView == null) {
            tableView = new TableView<>();
        }

//        loadProducts();
        orderDatePicker.setValue(LocalDate.now());
        branchComboBox.requestFocus();

        ObservableList<String> printers = FXCollections.observableArrayList(
                Printer.getAllPrinters().stream().map(Printer::getName).collect(Collectors.toList())
        );
        printerComboBox.setItems(printers);

        checkbox1.setOnAction(event -> {
            if (checkbox1.isSelected()) {
                checkbox2.setSelected(false);
                checkbox3.setSelected(false);
            }
        });

        checkbox2.setOnAction(event -> {
            if (checkbox2.isSelected()) {
                checkbox1.setSelected(false);
                checkbox3.setSelected(false);
            }
        });

        checkbox3.setOnAction(event -> {
            if (checkbox3.isSelected()) {
                checkbox1.setSelected(false);
                checkbox2.setSelected(false);
            }
        });
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    private void updateTotalQuantity() {
        double totalQuantity = 0;
        // Get the first 6 products in productTable1
        List<Product> products = productTable1.getItems();
        for (int i = 0; i < Math.min(7, products.size()); i++) {
            Product product = products.get(i);
            TextField quantityField = productQuantityMap.get(product.getId());

            if (quantityField != null && !quantityField.getText().isEmpty()) {

                try {
                    totalQuantity += Double.parseDouble(quantityField.getText());
                } catch (NumberFormatException e) {
                    // If quantity is not a valid number, ignore it
                }
            }
        }
        // Update the totalQuantityLabel
        totalQuantityLabel.setText("T0TAL -: " + totalQuantity);
    }

    private TableColumn<Product, Boolean> createCheckboxColumn() {
        TableColumn<Product, Boolean> checkboxColumn = new TableColumn<>("");

        checkboxColumn.setCellValueFactory(param -> {
            // Create a SimpleBooleanProperty bound to the product's selection state
            Product product = param.getValue();
            SimpleBooleanProperty selectedProperty = new SimpleBooleanProperty(product.isSelected());

            // Update the product when the selection state changes
            selectedProperty.addListener((observable, oldValue,
                                          newValue) -> product.setSelected(newValue));

            return selectedProperty;
        });

        checkboxColumn.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox00 = new CheckBox();

            {
                checkBox00.setOnAction(event -> {
                    Product product = getTableRow() != null ? getTableRow().getItem() : null;
                    if (product != null) {
                        product.setSelected(checkBox00.isSelected());
                    }
                });

                setGraphic(checkBox00);
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox00.setSelected(item);
                    setGraphic(checkBox00);
                }
            }
        });
        return checkboxColumn;
    }

    private double getQuantityFromCell(Product row) {
        TextField quantityField = productQuantityMap.get(row.getId());
        if (quantityField != null) {
            try {
                return Double.parseDouble(quantityField.getText());
            } catch (NumberFormatException e) {
                return 0; // Return 0 if the quantity is not a valid number
            }
        }
        return 0; // Return 0 if no quantity field is found
    }

    /**
     * Displays an informational alert message to the user.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Generates and prints an order summary with detailed product information across two pages.
     */
    @FXML
    private void printOrderSummary() {
        StringBuilder firstPageContent = new StringBuilder();
        StringBuilder secondPageContent = new StringBuilder();

        int orderId = Integer.parseInt(orderIdTextField.getText());
        List<OrderProduct> orderProducts = orderRepository.getProductsForOrder(orderId);
        Map<Integer, Double> productQuantityMap = orderProducts.stream()
                .collect(Collectors.toMap(  OrderProduct::getProductId, OrderProduct::getQuantity));

        // Get user details
        String username = usernameText.getText();
        String orderDate = orderDatePicker.getValue().toString();
        String selectedBranchName = branchComboBox.getValue();

        firstPageContent.append("<html>")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<title>Order Summary</title>")
                .append("<style>")
                .append("@page { size: auto; margin: 10px; }")
                .append("body { font-family: Arial, sans-serif; margin: 0; padding: 5px; font-size: 10px; width: 13cm;" +
                        " height: 40cm; }")
                .append("h2 { text-align: center; font-size: 16px; margin: 5px 0; }")
                .append(".order-details { margin: 10px 0; }")
                .append("table { width: 37%; border-collapse: collapse; }")
                .append("th, td { text-align: center; border: 1px solid #000; text-align: left; padding: 5px; font-size: 10px; }")
                .append("td.qty { text-align: center; width: 50px; }")
                .append("th { background-color: #f2f2f2; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div><strong>Order Id:</strong> ").append(orderId).append("</div>")
                .append("<div><strong>User:</strong> ").append(username).append("</div>")
                .append("<div><strong>Date:</strong> ").append(orderDate).append("</div>")
                .append("<div style='font-size: 12px;'>")
                .append(selectedBranchName != null ? "<strong>" + selectedBranchName + "</strong>" : "<strong>N/A</strong>")
                .append("</div>")
                .append("<table>")
                .append("<thead>")
                .append("</thead>")
                .append("<tbody>");

        // Gather products from all tables
        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(productTable1.getItems());
        allProducts.addAll(productTable2.getItems());
        allProducts.addAll(productTable3.getItems());

        // First 6 products
        double totalQuantityFirst6 = 0;
        for (int i = 0; i < Math.min(7, allProducts.size()); i++) {
            Product product = allProducts.get(i);
            double quantity = productQuantityMap.getOrDefault(product.getId(), 0.0);
            totalQuantityFirst6 += quantity;

            firstPageContent.append("<tr>")
                    .append("<td>").append(product.getProductName()).append("</td>")
                    .append("<td class='qty'>").append(formatQuantity(quantity));
            if (product.isSelected()) {
                firstPageContent.append("(Order)");
            }
            firstPageContent.append("</td></tr>");
        }

        // Add total for the first 6 products
        firstPageContent.append("<tr>")
                .append("<td><strong>තැ ටි ගණන </strong></td>")
                .append("<td class='qty'><strong>").append(formatQuantityForTotal(totalQuantityFirst6))
                .append("</strong></td>")
                .append("</tr>")
                .append("<tr><td colspan='2' style='height: 12px;'></td></tr>");

        // Remaining products
        if (allProducts.size() > 7) {
            for (int i = 7; i < allProducts.size(); i++) {
                Product product = allProducts.get(i);
                double quantity = productQuantityMap.getOrDefault(product.getId(), 0.0);

                firstPageContent.append("<tr>")
                        .append("<td>").append(product.getProductName()).append("</td>")
                        .append("<td class='qty'>").append(formatQuantity(quantity));

                if (i == 7 && quantity > 0) {
                    firstPageContent.append(" Kg");
                }
                if (product.isSelected()) {
                    firstPageContent.append("(Order)");
                }

                firstPageContent.append("</td></tr>");

                // Add spacing after the 16th product
                if (i == 16) {
                    firstPageContent.append("<tr><td colspan='2' style='height: 12px;'></td></tr>");
                }
            }
        }
        firstPageContent.append("</tbody>")
                .append("</table>")
                .append("</body>")
                .append("</html>");

        // Second page content
        secondPageContent.append("<html>")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<title>Order Summary</title>")
                .append("<style>")
                .append("@page { size: auto; margin: 10px; }")
                .append("body { font-family: Arial, sans-serif; margin: 0; padding: 5px; font-size: 10px;" +
                        " width: 13cm; height: 40cm; }")
                .append("h2 { text-align: center; font-size: 16px; margin: 5px 0; }")
                .append(".order-details { margin: 10px 0; }")
                .append("table { width: 37%; border-collapse: collapse; }")
                .append("th, td { text-align: center; border: 1px solid #000; text-align: left; padding: 5px;" +
                        " font-size: 10px; }")
                .append("td.qty { text-align: center; width: 50px; }")
                .append("th { background-color: #f2f2f2; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div><strong>Order ID:</strong> ").append(orderId).append("</div>")
                .append("<div><strong>User:</strong> ").append(username).append("</div>")
                .append("<div><strong>Date:</strong> ").append(orderDate).append("</div>")
                .append("<div style='font-size: 12px;'>")
                .append(selectedBranchName != null ? "<strong>" + selectedBranchName + "</strong>" :
                        "<strong>N/A</strong>")
                .append("</div>")
                .append("<table>")
                .append("<thead>")
                .append("</thead>")
                .append("<tbody>");

        List<Integer> selectedIndices = List.of(9, 10, 11, 19, 12, 15, 16, 17, 18, 14, 7,13, 8, 20, 21);
        for (int i : selectedIndices) {
            if (i < allProducts.size()) {
                Product product = allProducts.get(i);
                double quantity = productQuantityMap.getOrDefault(product.getId(), 0.0);

                secondPageContent.append("<tr>")
                        .append("<td>").append(product.getProductName()).append("</td>");

                // Specific handling for index 7
                if (i == 7 && quantity > 0) {
                    secondPageContent.append("<td class='qty'>").append(formatQuantity(quantity)).append(" Kg");
                    if (product.isSelected()) {
                        secondPageContent.append("(Order)");
                    }
                    secondPageContent.append("</td>");
                } else {
                    secondPageContent.append("<td class='qty'>").append(formatQuantity(quantity));
                    if (product.isSelected()) {
                        secondPageContent.append("(Order)");
                    }
                    secondPageContent.append("</td>");
                }
                secondPageContent.append("</tr>");
            }
        }
        secondPageContent.append("</tbody>")
                .append("</table>")
                .append("</body>")
                .append("</html>");

        Platform.runLater(() -> {
            // Get selected printer from ComboBox
            String selectedPrinterName = printerComboBox.getSelectionModel().getSelectedItem();

            if (selectedPrinterName != null && !selectedPrinterName.isEmpty()) {
                Printer selectedPrinter = Printer.getAllPrinters()
                        .stream()
                        .filter(p -> p.getName().equalsIgnoreCase(selectedPrinterName))
                        .findFirst()
                        .orElse(null);

                if (selectedPrinter != null) {

                    System.out.println("Printing on printer: " + selectedPrinter.getName());
                    try {
                        synchronized (this) {
                            printHTML1(secondPageContent.toString(), printerComboBox);
                            printHTML(firstPageContent.toString(), 4, printerComboBox);
                            System.out.println("Print job completed successfully.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error during printing: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Printer not found: " + selectedPrinterName);
                }
            } else {
                System.out.println("No printer selected.");
            }
        });
    }

    private void printHTML1(String secondPageContent, ComboBox<String> printerComboBox) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(secondPageContent);

        // Wait until content is fully loaded
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState,
                                                               newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Get content height dynamically
                double contentHeight = Double.parseDouble(webEngine.executeScript("document.body.scrollHeight")
                        .toString());

                // Get the selected printer name from the ComboBox
                String selectedPrinterName = printerComboBox.getSelectionModel().getSelectedItem();
                if (selectedPrinterName == null || selectedPrinterName.isEmpty()) {
                    System.out.println("No printer selected.");
                    return;
                }

                // Find the selected printer by name
                Printer printer = Printer.getAllPrinters()
                        .stream()
                        .filter(p -> p.getName().equalsIgnoreCase(selectedPrinterName)) // Match selected printer name
                        .findFirst()
                        .orElse(null);

                if (printer == null) {
                    System.out.println("Printer not found: " + selectedPrinterName);
                    return;
                }

                PrinterJob printerJob = PrinterJob.createPrinterJob(printer);
                if (printerJob != null) {


                    // Set custom page size and orientation
                    PageLayout pageLayout = printer.createPageLayout(
                            Paper.A4, PageOrientation.PORTRAIT, 10, 10, 10, 10
                    );

                    // Check if content fits on the page and print
                    boolean printed = printerJob.printPage(pageLayout, webView);
                    if (printed) {
                        printerJob.endJob();
                        System.out.println("Printed successfully.");
                    } else {
                        System.out.println("Print job failed.");
                    }
                } else {
                    System.out.println("Failed to create printer job.");
                }
            }
        });
    }

    private void printHTML(String firstPageContent, int pageCount, ComboBox<String> printerComboBox) {

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(firstPageContent);

        // Wait until content is fully loaded
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState,
                                                               newState) -> {
            if (newState == Worker.State.SUCCEEDED) {

                double contentHeight = Double.parseDouble(webEngine.executeScript("document.body.scrollHeight")
                        .toString());

                // Get the selected printer name from the ComboBox
                String selectedPrinterName = printerComboBox.getSelectionModel().getSelectedItem();
                if (selectedPrinterName == null || selectedPrinterName.isEmpty()) {
                    System.out.println("No printer selected.");
                    return;
                }

                // Find the selected printer by name
                Printer printer = Printer.getAllPrinters()
                        .stream()
                        .filter(p -> p.getName().equalsIgnoreCase(selectedPrinterName))
                        .findFirst()
                        .orElse(null);

                if (printer == null) {
                    System.out.println("Printer not found: " + selectedPrinterName);
                    return;
                }

                // Print the content for each page (based on pageCount)
                for (int i = 0; i < pageCount; i++) {
                    PrinterJob printerJob = PrinterJob.createPrinterJob(printer);
                    if (printerJob != null) {

                        PageLayout pageLayout = printer.createPageLayout(
                                Paper.A4, PageOrientation.PORTRAIT, 10, 10, 10, 10
                        );

                        // Print the WebView content
                        boolean printed = printerJob.printPage(pageLayout, webView);
                        if (printed) {
                            printerJob.endJob();
                            System.out.println("Printed page " + (i + 1) + " of " + pageCount);
                        } else {
                            System.out.println("Print job failed for page " + (i + 1));
                        }
                    } else {
                        System.out.println("Failed to create printer job.");
                    }
                }
            } else {
                System.out.println("Web content failed to load.");
            }
        });
    }
    private String formatQuantity(double quantity) {
        if (quantity == (int) quantity) {
            return String.valueOf((int) quantity); // Display as integer if no decimal places
        } else {
            return String.format("%.1f", quantity); // Display as decimal with two places
        }
    }

    private String formatQuantityForTotal(double totalQuantityFirst6) {
        if (totalQuantityFirst6 == (int) totalQuantityFirst6) {
            return String.valueOf((int) totalQuantityFirst6); // Display as integer if no decimal places
        } else {
            return String.format("%.1f", totalQuantityFirst6); // Display as decimal with two places
        }
    }

    @FXML
    private void printOrderSummary2() {
        StringBuilder secondPageContent = new StringBuilder();

        // Get user details
        String username = usernameText.getText();
        String orderDate = orderDatePicker.getValue().toString();
        String selectedBranchName = branchComboBox.getValue();

        // Gather products from all tables
        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(productTable1.getItems());
        allProducts.addAll(productTable2.getItems());
        allProducts.addAll(productTable3.getItems());

        // Get quantities from the order repository
        int orderId = Integer.parseInt(orderIdTextField.getText());
        List<OrderProduct> orderProducts = orderRepository.getProductsForOrder(orderId);
        Map<Integer, Double> productQuantityMap = orderProducts.stream()
                .collect(Collectors.toMap(OrderProduct::getProductId, OrderProduct::getQuantity));

        // Second page content
        secondPageContent.append("<html>")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<title>Order Summary</title>")
                .append("<style>")
                .append("@page { size: auto; margin: 10px; }")
                .append("body { font-family: Arial, sans-serif; margin: 0; padding: 5px; font-size: 10px; " +
                        "width: 13cm; height: 40cm; }")
                .append("h2 { text-align: center; font-size: 16px; margin: 5px 0; }")
                .append(".order-details { margin: 10px 0; }")
                .append("table { width: 37%; border-collapse: collapse; }")
                .append("th, td { text-align: center; border: 1px solid #000; text-align: left; padding: 5px;" +
                        " font-size: 10px; }")
                .append("td.qty { text-align: center; width: 50px; }")
                .append("th { background-color: #f2f2f2; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div><strong>Order Id:</strong> ").append(orderId).append("</div>")
                .append("<div><strong>User:</strong> ").append(username).append("</div>")
                .append("<div><strong>Date:</strong> ").append(orderDate).append("</div>")
                .append("<div style='font-size: 12px;'>")
                .append(selectedBranchName != null ? "<strong>" + selectedBranchName + "</strong>" :
                        "<strong>N/A</strong>")
                .append("</div>")
                .append("<table>")
                .append("<thead>")
                .append("</thead>")
                .append("<tbody>");

        List<Integer> selectedIndices = List.of(9, 10, 11, 19, 12, 15, 16, 17, 18, 14, 7, 13, 8, 20, 21);
        for (int i : selectedIndices) {
            if (i < allProducts.size()) {
                Product product = allProducts.get(i);
                double quantity = productQuantityMap.getOrDefault(product.getId(), 0.0); // Get correct quantity

                secondPageContent.append("<tr>")
                        .append("<td>").append(product.getProductName()).append("</td>");

                // Specific handling for index 7
                if (i == 7 && quantity > 0) {
                    secondPageContent.append("<td class='qty'>").append(formatQuantity(quantity)).append(" Kg");
                    if (product.isSelected()) {
                        secondPageContent.append("(Order)");
                    }
                    secondPageContent.append("</td>");
                } else {
                    secondPageContent.append("<td class='qty'>").append(formatQuantity(quantity));
                    if (product.isSelected()) {
                        secondPageContent.append("(Order)");
                    }
                    secondPageContent.append("</td>");
                }
                secondPageContent.append("</tr>");
            }
        }
        secondPageContent.append("</tbody>")
                .append("</table>")
                .append("</body>")
                .append("</html>");

        Platform.runLater(() -> {
            // Get selected printer from ComboBox
            String selectedPrinterName = printerComboBox.getSelectionModel().getSelectedItem();

            if (selectedPrinterName != null && !selectedPrinterName.isEmpty()) {
                Printer selectedPrinter = Printer.getAllPrinters()
                        .stream()
                        .filter(p -> p.getName().equalsIgnoreCase(selectedPrinterName))
                        .findFirst()
                        .orElse(null);

                if (selectedPrinter != null) {
                    System.out.println("Printing on printer: " + selectedPrinter.getName());
                    try {
                        synchronized (this) {
                            printHTML1(secondPageContent.toString(), printerComboBox);
                            System.out.println("Print job completed successfully.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error during printing: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Printer not found: " + selectedPrinterName);
                }
            } else {
                System.out.println("No printer selected.");
            }
        });
    }


    private void printOrderSummary3() {
        StringBuilder firstPageContent = new StringBuilder();

        // Get user details
        String username = usernameText.getText();
        String orderDate = orderDatePicker.getValue().toString();
        String selectedBranchName = branchComboBox.getValue();
        int orderId = Integer.parseInt(orderIdTextField.getText());


        firstPageContent.append("<html>")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<title>Order Summary</title>")
                .append("<style>")
                .append("@page { size: auto; margin: 10px; }")
                .append("body { font-family: Arial, sans-serif; margin: 0; padding: 5px; font-size: 10px; width: 13cm;" +
                        " height: 40cm; }")
                .append("h2 { text-align: center; font-size: 16px; margin: 5px 0; }")
                .append(".order-details { margin: 10px 0; }")
                .append("table { width: 37%; border-collapse: collapse; }")
                .append("th, td { text-align: center; border: 1px solid #000; text-align: left; padding: 5px; font-size: 10px; }")
                .append("td.qty { text-align: center; width: 50px; }")
                .append("th { background-color: #f2f2f2; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div><strong>Order Id:</strong> ").append(orderId).append("</div>")
                .append("<div><strong>User:</strong> ").append(username).append("</div>")
                .append("<div><strong>Date:</strong> ").append(orderDate).append("</div>")
                .append("<div style='font-size: 12px;'>")
                .append(selectedBranchName != null ? "<strong>" + selectedBranchName + "</strong>" : "<strong>N/A</strong>")
                .append("</div>")
                .append("<table>")
                .append("<thead>")
                .append("</thead>")
                .append("<tbody>");

        // Gather products from all tables
        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(productTable1.getItems());
        allProducts.addAll(productTable2.getItems());
        allProducts.addAll(productTable3.getItems());

        // Get product quantity from the order
        Map<Integer, Double> productQuantityMap = new HashMap<>();
        List<OrderProduct> orderProducts = orderRepository.getProductsForOrder(Integer.parseInt(orderIdTextField.getText()));
        for (OrderProduct orderProduct : orderProducts) {
            productQuantityMap.put(orderProduct.getProductId(), orderProduct.getQuantity());
        }

        // First 6 products
        double totalQuantityFirst6 = 0;
        for (int i = 0; i < Math.min(7, allProducts.size()); i++) {
            Product product = allProducts.get(i);
            double quantity = productQuantityMap.getOrDefault(product.getId(), 0.0);
            totalQuantityFirst6 += quantity;

            firstPageContent.append("<tr>")
                    .append("<td>").append(product.getProductName()).append("</td>")
                    .append("<td class='qty'>").append(formatQuantity(quantity));
            if (product.isSelected()) {
                firstPageContent.append("(Order)");
            }
            firstPageContent.append("</td></tr>");
        }

        // Add total for the first 6 products
        firstPageContent.append("<tr>")
                .append("<td><strong>තැ ටි ගණන </strong></td>")
                .append("<td class='qty'><strong>").append(formatQuantityForTotal(totalQuantityFirst6))
                .append("</strong></td>")
                .append("</tr>")
                .append("<tr><td colspan='2' style='height: 12px;'></td></tr>");

        // Remaining products
        if (allProducts.size() > 7) {
            for (int i = 7; i < allProducts.size(); i++) {
                Product product = allProducts.get(i);
                double quantity = productQuantityMap.getOrDefault(product.getId(), 0.0);

                firstPageContent.append("<tr>")
                        .append("<td>").append(product.getProductName()).append("</td>")
                        .append("<td class='qty'>").append(formatQuantity(quantity));

                if (i == 7 && quantity > 0) {
                    firstPageContent.append(" Kg");
                }
                if (product.isSelected()) {
                    firstPageContent.append("(Order)");
                }

                firstPageContent.append("</td></tr>");

                // Add spacing after the 16th product
                if (i == 16) {
                    firstPageContent.append("<tr><td colspan='2' style='height: 12px;'></td></tr>");
                }
            }
        }
        firstPageContent.append("</tbody>")
                .append("</table>")
                .append("</body>")
                .append("</html>");

        Platform.runLater(() -> {
            // Get selected printer from ComboBox
            String selectedPrinterName = printerComboBox.getSelectionModel().getSelectedItem();

            if (selectedPrinterName != null && !selectedPrinterName.isEmpty()) {
                Printer selectedPrinter = Printer.getAllPrinters()
                        .stream()
                        .filter(p -> p.getName().equalsIgnoreCase(selectedPrinterName))
                        .findFirst()
                        .orElse(null);

                if (selectedPrinter != null) {

                    System.out.println("Printing on printer: " + selectedPrinter.getName());
                    try {
                        synchronized (this) {
                            printHTML(firstPageContent.toString(), 1, printerComboBox);
                            System.out.println("Print job completed successfully.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error during printing: " + e.getMessage());
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("Printer not found: " + selectedPrinterName);
                }
            } else {
                System.out.println("No printer selected.");
            }
        });
    }
    
    @FXML
    private void printOrder(ActionEvent event) {
        printOrderSummary2();
    }

    @FXML
    private void printOrder2(ActionEvent event) {
        printOrderSummary3();
    }

    @FXML
    private void logout(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double windowX = stage.getX();
        double windowY = stage.getY();

        usernameLabel.setText("");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/foodordersystem/Login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setX(windowX);
            loginStage.setY(windowY);
            loginStage.centerOnScreen();
            loginStage.show();

            stage.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading login screen.");
            showAlert("Error", "Failed to load login screen.");
        }
    }

    public void loadOrderDetails(Order order) {
        if (order != null) {
            // Set order date
            orderDatePicker.setValue(order.getOrderDate());

            // Fetch user details
            User user = userRepository.getUserById(order.getUserId());
            String userName = (user != null) ? user.getUserName() : "Unknown User";
            usernameText.setText(userName);

            // Fetch branch details
            Branch branch = branchRepository.getBranchById(order.getBranchId());
            if (branch != null && branchComboBox != null) {
                ObservableList<String> branchNames = FXCollections.observableArrayList();
                branchNames.add(branch.getBranchName());
                branchComboBox.setItems(branchNames);
                branchComboBox.getSelectionModel().select(branch.getBranchName());
            } else {
                System.out.println("Error: branchComboBox is null or branch not found.");
            }

            // Load product details
            loadProductsForOrder(order.getId());
        }
    }

    private void loadProductsForOrder(int orderId) {
        List<Product> allProducts = productService.getAllProducts();

        orderIdTextField.setText(String.valueOf(orderId));

        List<OrderProduct> orderProducts = orderRepository.getProductsForOrder(orderId);
        System.out.println(orderId);
        Map<Integer, Double> productQuantityMap = orderProducts.stream()
                .collect(Collectors.toMap(OrderProduct::getProductId, OrderProduct::getQuantity));

        ObservableList<Product> productList1 = FXCollections.observableArrayList();
        ObservableList<Product> productList2 = FXCollections.observableArrayList();
        ObservableList<Product> productList3 = FXCollections.observableArrayList();

        int totalProducts = allProducts.size();
        int productsPerTable = 14;

        // Distribute the products across the three tables
        if (totalProducts > 0) {
            productList1.addAll(allProducts.subList(0, Math.min(productsPerTable, totalProducts)));
        }
        if (totalProducts > productsPerTable) {
            productList2.addAll(allProducts.subList(productsPerTable, Math.min(productsPerTable * 2, totalProducts)));
        }
        if (totalProducts > productsPerTable * 2) {
            productList3.addAll(allProducts.subList(productsPerTable * 2, Math.min(productsPerTable * 3, totalProducts)));
        }

        // Set the items for each table
        productTable1.setItems(productList1);
        productTable2.setItems(productList2);
        productTable3.setItems(productList3);

        // Create quantity columns
        TableColumn<Product, String> quantityColumn1 = createQuantityColumn(productQuantityMap);
        TableColumn<Product, String> quantityColumn2 = createQuantityColumn(productQuantityMap);
        TableColumn<Product, String> quantityColumn3 = createQuantityColumn(productQuantityMap);

        // Create checkbox columns
        TableColumn<Product, Boolean> checkboxColumn1 = createCheckboxColumn();
        TableColumn<Product, Boolean> checkboxColumn2 = createCheckboxColumn();
        TableColumn<Product, Boolean> checkboxColumn3 = createCheckboxColumn();

        // Add the columns to the tables
        productTable1.getColumns().addAll(checkboxColumn1, quantityColumn1);
        productTable2.getColumns().addAll(checkboxColumn2, quantityColumn2);
        productTable3.getColumns().addAll(checkboxColumn3, quantityColumn3);
    }

    private TableColumn<Product, String> createQuantityColumn(Map<Integer, Double> productQuantityMap) {
        TableColumn<Product, String> quantityField = new TableColumn<>("Quantity");


        quantityField.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            double quantity = productQuantityMap.getOrDefault(product.getId(), 0.0);
            return new SimpleStringProperty(String.valueOf(quantity));
        });

        quantityField.setCellFactory(TextFieldTableCell.forTableColumn());
        quantityField.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            double newQuantity = Double.parseDouble(event.getNewValue());
            productQuantityMap.put(product.getId(), newQuantity); // Update the map with new quantity
        });

        return quantityField;
    }

    public void updateStatusButton(ActionEvent actionEvent) {
        int orderId = Integer.parseInt(orderIdTextField.getText()); // Get Order ID
        String selectedOption = getSelectedOption(); // Get selected option
        printOrderSummary();
        boolean isUpdated = orderService.updateOrderStatusAndOption(orderId, 1, selectedOption); // Update status to 1

        /*if (isUpdated) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Order status and option updated successfully!", ButtonType.OK);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update order.", ButtonType.OK);
            alert.showAndWait();
        }*/
    }

    /**
     * Retrieve the selected option from checkboxes
     */
    private String getSelectedOption() {
        if (checkbox1.isSelected()) {
            return "ළග කඩ";
        } else if (checkbox2.isSelected()) {
            return "දුර කඩ";
        } else if (checkbox3.isSelected()) {
            return "අපේ කඩ";
        }
        return "0"; // Default if no checkbox is selected
    }
}