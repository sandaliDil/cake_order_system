package com.example.foodordersystem.controller;

import com.example.foodordersystem.Session;
import com.example.foodordersystem.database.DatabaseConnection;
import com.example.foodordersystem.model.*;
import com.example.foodordersystem.repository.OrderRepository;
import com.example.foodordersystem.service.BranchService;
import com.example.foodordersystem.service.OrderService;
import com.example.foodordersystem.service.ProductService;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.*;

import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import static kotlin.text.Typography.nbsp;


public class OrderController {

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
    private CheckBox morning;
    @FXML
    private CheckBox afternoon;
    @FXML
    private ComboBox<String> printerComboBox;
    @FXML
    private Label totalQuantityLabel;
    @FXML
    private Label notificationLabel;
    @FXML
    private AnchorPane notificationBar;
    @FXML
    private Button closeNotificationBtn;

    private final OrderRepository orderRepository = new OrderRepository();
    private BranchService branchService;
    private ProductService productService;
    private OrderService orderService;
    private ObservableList<String> branchNames;

    private Map<Integer, TextField> productQuantityMap = new HashMap<>();

    public OrderController() {

        this.productService = new ProductService();
        this.branchService = new BranchService();
        this.orderService = new OrderService();
    }

    @FXML
    public void initialize() {

        populateBranchComboBox();
        productNameColumn1.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productNameColumn2.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productNameColumn3.setCellValueFactory(new PropertyValueFactory<>("productName"));

        if (tableView == null) {
            tableView = new TableView<>();
        }
        TableColumn<Product, String> quantityColumn = createQuantityColumn();
        tableView.getColumns().add(quantityColumn);
        tableView.setRowFactory(tv -> new TableRow<>());
        loadProducts();
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

        morning.setOnAction(event -> {
            if (morning.isSelected()) {
                afternoon.setSelected(false);
            }
        });

        afternoon.setOnAction(event -> {
            if (afternoon.isSelected()) {
                morning.setSelected(false);
            }
        });

        updatePendingOrdersNotification();
        startOrderNotificationChecker();

        // Hide notification when clicking close button
        closeNotificationBtn.setOnAction(event -> hideNotification());

       // searchOrderButton.setOnAction(event -> searchOrderById());
    }

    private void updatePendingOrdersNotification() {
        int pendingOrders = orderRepository.countPendingOrders();

        Platform.runLater(() -> {
            if (pendingOrders > 0) {
                notificationLabel.setText("🚀 Pending Orders: " + pendingOrders);
                notificationLabel.setCursor(Cursor.HAND); // Change cursor to indicate clickable
                showNotification();

                // Add event handler to open OnlineOrders.fxml
                notificationLabel.setOnMouseClicked(event -> openOnlineOrdersPage());
            } else {
                hideNotification();
                notificationLabel.setOnMouseClicked(null); // Remove click event if no pending orders
            }
        });
    }

    private void openOnlineOrdersPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/foodordersystem/OnlineOrders.fxml"));
            Parent root = loader.load();

            // Create a new Stage (window)
            Stage onlineOrdersStage = new Stage();
            onlineOrdersStage.setTitle("Online Orders");
            onlineOrdersStage.setScene(new Scene(root));
            onlineOrdersStage.initModality(Modality.WINDOW_MODAL); // Ensures it's a pop-up
            onlineOrdersStage.initOwner(notificationLabel.getScene().getWindow()); // Set parent window
            onlineOrdersStage.show();

            System.out.println("OnlineOrders.fxml opened in a new window");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading OnlineOrders.fxml");
        }
    }

    private void showNotification() {
        notificationBar.setVisible(true);
        notificationBar.setMaxWidth(100);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), notificationBar);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void hideNotification() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationBar);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> notificationBar.setVisible(false));
        fadeOut.play();
    }

    private void startOrderNotificationChecker() {
        Thread notificationThread = new Thread(() -> {
            while (true) {
                updatePendingOrdersNotification();
                try {
                    Thread.sleep(10000); // Check every 10 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        notificationThread.setDaemon(true);
        notificationThread.start();
    }

    /**
     * Populates the branchComboBox with branch names retrieved from the branchService.
     */
    private void populateBranchComboBox() {

        List<Branch> branches = branchService.getAllBranches();
        branchNames = FXCollections.observableArrayList();
        for (Branch branch : branches) {
            branchNames.add(branch.getBranchName());
        }

        // Create a FilteredList with a complex filtering predicate
        FilteredList<String> filteredBranches = new FilteredList<>(branchNames, s -> true);
        branchComboBox.setItems(filteredBranches);

        // Add a listener to filter branches based on input
        branchComboBox.getEditor().textProperty().addListener((observable,
                                                               oldValue, newValue) -> {
            filteredBranches.setPredicate(branchName -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Show all branches when filter text is empty
                }

                // Convert input and branch name to lower case for case-insensitive comparison
                String lowerCaseBranchName = branchName.toLowerCase();
                String lowerCaseNewValue = newValue.toLowerCase();

                // Filter branches that contain the input string (substring match)
                boolean containsMatch = lowerCaseBranchName.contains(lowerCaseNewValue);

                boolean multiWordMatch = Arrays.stream(lowerCaseNewValue.split("\\s+"))
                        .allMatch(keyword -> lowerCaseBranchName.contains(keyword));

                // Return true if either filter condition is met
                return containsMatch || multiWordMatch;
            });

            // Optionally show the dropdown if filtering
            branchComboBox.show();
        });
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    /**
     * Loads products into three separate tables (productTable1, productTable2, and productTable3).
     */
    private void loadProducts() {
        List<Product> allProducts = productService.getAllProducts();

        ObservableList<Product> productList1 = FXCollections.observableArrayList();
        ObservableList<Product> productList2 = FXCollections.observableArrayList();
        ObservableList<Product> productList3 = FXCollections.observableArrayList();

        int totalProducts = allProducts.size();
        int productsPerTable = 10;

        // Distribute the products across the three tables
        if (totalProducts > 0) {
            productList1.addAll(allProducts.subList(0, Math.min(productsPerTable, totalProducts)));
        }
        if (totalProducts > productsPerTable) {
            productList2.addAll(allProducts.subList(productsPerTable, Math.min(productsPerTable * 2, totalProducts)));
        }
        if (totalProducts > productsPerTable * 2) {
            productList3.addAll(allProducts.subList(productsPerTable * 2, Math.min(productsPerTable * 3,
                    totalProducts)));
        }

        // Set the items for each table
        productTable1.setItems(productList1);
        productTable2.setItems(productList2);
        productTable3.setItems(productList3);

        // Create quantity columns
        TableColumn<Product, String> quantityColumn1 = createQuantityColumn();
        TableColumn<Product, String> quantityColumn2 = createQuantityColumn();
        TableColumn<Product, String> quantityColumn3 = createQuantityColumn();

        // Create checkbox columns
        TableColumn<Product, Boolean> checkboxColumn1 = createCheckboxColumn();
        TableColumn<Product, Boolean> checkboxColumn2 = createCheckboxColumn();
        TableColumn<Product, Boolean> checkboxColumn3 = createCheckboxColumn();

        // Add the columns to the tables
        productTable1.getColumns().addAll(checkboxColumn1, quantityColumn1);
        productTable2.getColumns().addAll(checkboxColumn2, quantityColumn2);
        productTable3.getColumns().addAll(checkboxColumn3, quantityColumn3);
    }

    private void clearCheckboxes(TableView<Product> table) {
        for (Product product : table.getItems()) {
            product.setSelected(false);
        }

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

    private TableColumn<Product, String> createQuantityColumn() {
        TableColumn<Product, String> quantityColumn = new TableColumn<>("Quantity");

        quantityColumn.setCellFactory(column -> new TableCell<>() {
            private final TextField quantityField = new TextField();

            {
                quantityField.setStyle("-fx-background-color: lightyellow; -fx-font-size: 16px;" +
                        " -fx-background-radius: 15px 15px 15px 15px;");
                quantityField.setPrefWidth(60);

                // Allow only double values (digits and a single dot)
                quantityField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
                    String input = event.getCharacter();
                    String currentText = quantityField.getText();

                    // Allow digits, a single dot (if not already present), and block invalid input
                    if (!input.matches("\\d") && !input.equals(".") ||
                            (input.equals(".") && currentText.contains("."))) {
                        event.consume();
                    }
                });

                quantityField.setEditable(true);
                setGraphic(quantityField);

                quantityField.setOnKeyPressed(event -> {
                    TableView<Product> tableView = getTableView();
                    int currentIndex = getIndex();
                    int nextIndex = currentIndex + 1;
                    int previousIndex = currentIndex - 1;

                    switch (event.getCode()) {
                        case ENTER:
                            // Validate or default the quantityField value
                            if (quantityField.getText().isEmpty()) {
                                quantityField.setText("0");
                            }

                            // If not the last row, move to the next row in the same table
                            if (nextIndex < tableView.getItems().size()) {
                                focusCell(nextIndex, quantityColumn);
                            } else {
                                // Move to the first row of the next table's Quantity column
                                TableView<Product> nextTable = getNextTable(tableView);
                                if (nextTable != null) {
                                    focusCell(0, getQuantityColumn(nextTable));
                                }
                            }
                            break;
                        case UP:
                            if (previousIndex >= 0) {
                                focusCell(previousIndex, quantityColumn);
                            } else {
                                // Move to the last row of the previous table
                                TableView<Product> previousTable = getPreviousTable(tableView);
                                if (previousTable != null) {
                                    int lastRowIndex = previousTable.getItems().size() - 1;
                                    focusCell(lastRowIndex, getQuantityColumn(previousTable));
                                }
                            }
                            break;

                        case DOWN:
                            if (nextIndex < tableView.getItems().size()) {
                                focusCell(nextIndex, quantityColumn);
                            } else {
                                // Move to the first row of the next table
                                TableView<Product> nextTable = getNextTable(tableView);
                                if (nextTable != null) {
                                    focusCell(0, getQuantityColumn(nextTable));
                                }
                            }
                            break;

                        case LEFT:
                            navigateToPreviousColumn(tableView, currentIndex);
                            break;

                        case RIGHT:
                            navigateToNextColumn(tableView, currentIndex);
                            break;
                    }
                });

                quantityField.textProperty().addListener((observable, oldValue,
                                                          newValue) -> {
                    Product product = getTableRow() != null ? getTableRow().getItem() : null;
                    if (product != null) {
                        productQuantityMap.put(product.getId(), quantityField);
                    }
                    // Call the method to update the total quantity
                    updateTotalQuantity();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    if (item != null) {
                        quantityField.setText(item);
                    } else {
                        quantityField.setText("");
                    }
                    setGraphic(quantityField);
                }
            }
        });
        return quantityColumn;
    }

    /**
     * Focuses on the given cell in the table column.
     */
    private void focusCell(int rowIndex, TableColumn<Product, ?> column) {
        TableCell<Product, ?> cell = getCellAt(rowIndex, column);
        if (cell != null) {
            TextField field = (TextField) cell.getGraphic();
            if (field != null) {
                field.requestFocus();
            }
        }
    }
    private TableView<Product> getPreviousTable(TableView<Product> currentTable) {
        if (currentTable == productTable3) {
            return productTable2;
        } else if (currentTable == productTable2) {
            return productTable1;
        }
        return null;
    }
    private TableView<Product> getNextTable(TableView<Product> currentTable) {
        if (currentTable == productTable1) {
            return productTable2;
        } else if (currentTable == productTable2) {
            return productTable3;
        }
        return null;
    }

    private TableColumn<Product, ?> getQuantityColumn(TableView<Product> table) {
        return table.getColumns().stream()
                .filter(column -> "Quantity".equals(column.getText()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Moves focus to the previous column if available.
     */
    private void navigateToPreviousColumn(TableView<Product> tableView, int rowIndex) {
        int currentColumnIndex = tableView.getColumns().indexOf(rowIndex);
        if (currentColumnIndex > 0) {
            TableColumn<Product, ?> previousColumn = tableView.getColumns().get(currentColumnIndex - 1);
            focusCell(rowIndex, previousColumn);
        }
    }

    /**
     * Moves focus to the next column if available.
     */
    private void navigateToNextColumn(TableView<Product> tableView, int rowIndex) {
        int currentColumnIndex = tableView.getColumns().indexOf(rowIndex);
        if (currentColumnIndex < tableView.getColumns().size() - 1) {
            TableColumn<Product, ?> nextColumn = tableView.getColumns().get(currentColumnIndex + 1);
            focusCell(rowIndex, nextColumn);
        }
    }

    /**
     * Returns the TableCell at a specific row and column.
     */
    private TableCell<Product, ?> getCellAt(int rowIndex, TableColumn<Product, ?> column) {
        TableView<Product> tableView = column.getTableView();
        for (Node node : tableView.lookupAll(".table-row-cell")) {
            if (node instanceof TableRow<?> row && row.getIndex() == rowIndex) {
                for (Node cellNode : row.lookupAll(".table-cell")) {
                    if (cellNode instanceof TableCell<?, ?> cell && column.equals(cell.getTableColumn())) {
                        return (TableCell<Product, ?>) cell;
                    }
                }
            }
        }
        return null;
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


    /**
     * Saves the order after validating user inputs and capturing all relevant order details.
     */
    @FXML
    private int
    saveOrder(ActionEvent event) {
        String username = usernameLabel.getText();
        System.out.println("Username: " + username);

        User loggedInUser = Session.getInstance().getLoggedInUser();
        if (loggedInUser == null) {
            System.out.println("No user is logged in.");
            showAlert("Error", "Please log in to place an order.");
            return -1;
        }

        Order order = new Order();
        order.setUserId(loggedInUser.getId());

        // Validate branch selection
        String selectedBranchName = branchComboBox.getValue();
        if (selectedBranchName == null) {
            System.out.println("Branch is not selected.");
            showAlert("Error", "Please select a branch.");
            return -1;
        }

        Branch selectedBranch = getBranchByName(selectedBranchName);
        if (selectedBranch == null) {
            System.out.println("Branch not found.");
            showAlert("Error", "Selected branch is not valid.");
            return -1;
        }

        order.setBranchId(selectedBranch.getId());

        // Validate order date
        if (orderDatePicker.getValue() == null) {
            System.out.println("Order date is not selected.");
            showAlert("Error", "Please select an order date.");
            return -1;
        }
        order.setOrderDate(orderDatePicker.getValue().atStartOfDay());

        // Validate time range selection
        String selectedTime = null;
        if (morning.isSelected()) {
            selectedTime = "morning";
        } else if (afternoon.isSelected()) {
            selectedTime = "afternoon";
        }

        if (selectedTime == null) {
            System.out.println("Time range is not selected.");
            showAlert("Error", "Please select a time range (Morning or Afternoon).");
            return -1;
        }
        order.setTimeRange(selectedTime);

        // Capture the selected option (Checkbox selection)
        String selectedOption = "0";  // Default value
        if (checkbox1.isSelected()) {
            selectedOption = "ළග කඩ";
        } else if (checkbox2.isSelected()) {
            selectedOption = "දුර කඩ";
        } else if (checkbox3.isSelected()) {
            selectedOption = "අපේ කඩ";
        }
        order.setOption(selectedOption);

        boolean orderExists = orderService.checkOrderExists(selectedBranch.getId(), order.getOrderDate(), order.getTimeRange());
        if (orderExists) {
            System.out.println("An order for this branch already exists on the same day.");
            showAlert("Error", "An order for this branch has already been placed today.");
            return -1;  // Return -1 to prevent saving the same order
        }

        // Validate and add order products
        List<OrderProduct> orderProducts = new ArrayList<>();
        if (!addOrderProductsFromTable(productTable1, orderProducts) ||
                !addOrderProductsFromTable(productTable2, orderProducts) ||
                !addOrderProductsFromTable(productTable3, orderProducts)) {
            System.out.println("Invalid quantity in order.");
            showAlert("Error", "Please enter valid quantities for all products.");
            return -1;
        }

        order.setItems(orderProducts);
        order.setStatus(true);

        // Save the order
        int orderId = orderService.saveOrder(order);

        if (orderId != -1) {
            System.out.println("Order saved successfully with ID: " + orderId);
            showAlert("Success", "Order saved successfully. Order ID: " + orderId);
            return orderId;
        } else {
            showAlert("Error", "Failed to save the order. Please try again.");
            return -1;
        }
    }

    private boolean addOrderProductsFromTable(TableView<Product> productTable, List<OrderProduct> orderProducts) {
        boolean isValid = true;
        for (Product row : productTable.getItems()) {
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProductId(row.getId());

            // Get quantity from the corresponding TextField
            double quantity = (double) getQuantityFromCell(row);
            if (quantity < 0) {
                isValid = false;  // If quantity is invalid, mark the validation as failed
            }
            orderProduct.setQuantity((double) quantity);
            orderProducts.add(orderProduct); // Add the order product to the list
        }
        return isValid;
    }

    private Branch getBranchByName(String branchName) {
        for (Branch branch : branchService.getAllBranches()) { // Replace with your actual list or method
            if (branch.getBranchName().equals(branchName)) {
                return branch;
            }
        }
        return null; // Return null if no match found
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
    private void printOrderSummary(int orderId) {
        StringBuilder firstPageContent = new StringBuilder();
        StringBuilder secondPageContent = new StringBuilder();

        String selectedTime = null;
        if (morning.isSelected()) {
            selectedTime = "Morning";
        } else if (afternoon.isSelected()) {
            selectedTime = "Afternoon";
        }

        // Get user details
        String username = usernameLabel.getText();
        String orderDate = orderDatePicker.getValue().toString();
        String selectedBranchName = branchComboBox.getValue();

        // Build the first page (Narrow paper size)
        firstPageContent.append("<html>")
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
                .append("th, td { text-align: center; border: 1px solid #000; text-align: left; " +
                        "padding: 5px; font-size: 10px; }")
                .append("td.qty { text-align: center; width: 50px; }")
                .append("th { background-color: #f2f2f2; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div><strong>Order ID:</strong> ").append(orderId).append("</div>")
                .append("<div><strong>User:</strong> ").append(username).append("</div>")
                .append("<div><strong>Date:</strong> ").append(orderDate).append("&nbsp;&nbsp;(").append(selectedTime != null ? selectedTime : "N/A").append(")").append("</div>")
                .append("<div style='font-size: 12px;'>")
                .append(selectedBranchName != null ? "<strong>" + selectedBranchName + "</strong>" :
                        "<strong>N/A</strong>")
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
            double quantity = getQuantityFromCell(product);
            totalQuantityFirst6 += quantity;

            firstPageContent.append("<tr>")
                    .append("<td>").append(product.getProductName()).append("</td>")
                    .append("<td class='qty'>").append(formatQuantity(quantity));
            if (product.isSelected()) {
                firstPageContent.append("(Order)");
            }
            firstPageContent .append("</td>")
                            .append("</tr>");
        }

        // Add total for the first 6 products
        firstPageContent.append("<tr>")
                .append("<td><strong>තැ ටි ගණන </strong></td>")
                .append("<td class='qty'><strong>").append(formatQuantityForTotal(totalQuantityFirst6)).
                append("</strong></td>")
                .append("</tr>")
                .append("<tr><td colspan='2' style='height: 12px;'></td></tr>");

        // Remaining products
        if (allProducts.size() > 7) {
            for (int i = 7; i < allProducts.size(); i++) {
                Product product = allProducts.get(i);
                double quantity = getQuantityFromCell(product);

                firstPageContent.append("<tr>")
                        .append("<td>").append(product.getProductName()).append("</td>")
                        .append("<td class='qty'>").append(formatQuantity(quantity));

                // Add "Kg" for the first product on the second page and "Order" if selected
                if (i == 7 && quantity > 0) {
                    firstPageContent.append(" Kg");
                }
                if (product.isSelected()) {
                    firstPageContent.append("(Order)");
                }

                firstPageContent.append("</td>")
                        .append("</tr>");

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
                .append("<div><strong>Date:</strong> ").append(orderDate).append("&nbsp;&nbsp;(").append(selectedTime != null ? selectedTime : "N/A").append(")").append("</div>")
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
                double quantity = getQuantityFromCell(product);

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
                            System.out.println("xxx" + printerComboBox);
                            System.out.println("i hate u "+secondPageContent);
                            printHTML(firstPageContent.toString(), 4, printerComboBox);
                            System.out.println("xxx1" + printerComboBox);
                            System.out.println("fuq" + firstPageContent);
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

    }

    private void printHTML1(String secondPageContent, ComboBox<String> printerComboBox) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(secondPageContent);

        System.out.println(secondPageContent);



        // Wait until content is fully loaded
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState,
                                                               newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Get content height dynamically
//                double contentHeight = Double.parseDouble(webEngine.executeScript("document.body.scrollHeight")
//                        .toString());

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

                        // Set default page size and orientation
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
        String username = usernameLabel.getText();
        String orderDate = orderDatePicker.getValue().toString();
        String selectedBranchName = branchComboBox.getValue();

        String selectedTime = null;
        if (morning.isSelected()) {
            selectedTime = "Morning";
        } else if (afternoon.isSelected()) {
            selectedTime = "Afternoon";
        }


        // Gather products from all tables
        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(productTable1.getItems());
        allProducts.addAll(productTable2.getItems());
        allProducts.addAll(productTable3.getItems());

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
                // .append("<div><strong>Order ID:</strong> ").append(orderId).append("</div>")
                .append("<div><strong>User:</strong> ").append(username).append("</div>")
                .append("<div><strong>Date:</strong> ").append(orderDate).append("&nbsp;&nbsp;(").append(selectedTime != null ? selectedTime : "N/A").append(")").append("</div>")
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
                double quantity = getQuantityFromCell(product);

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


    }

    @FXML
    private void printOrderSummary3() {
        StringBuilder firstPageContent = new StringBuilder();

        // Get user details
        String username = usernameLabel.getText();
        String orderDate = orderDatePicker.getValue().toString();
        String selectedBranchName = branchComboBox.getValue();

        String selectedTime = null;
        if (morning.isSelected()) {
            selectedTime = "Morning";
        } else if (afternoon.isSelected()) {
            selectedTime = "Afternoon";
        }

        firstPageContent.append("<html>")
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
                .append("<div><strong>User:</strong> ").append(username).append("</div>")
                .append("<div><strong>Date:</strong> ").append(orderDate).append("&nbsp;&nbsp;(").append(selectedTime != null ? selectedTime : "N/A").append(")").append("</div>")
                .append("<div style='font-size: 12px;'>")
                .append(selectedBranchName != null ? "<strong>" + selectedBranchName + "</strong>" :
                        "<strong>N/A</strong>")
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
            double quantity = getQuantityFromCell(product);
            totalQuantityFirst6 += quantity;
            firstPageContent.append("<tr>")
                    .append("<td>").append(product.getProductName()).append("</td>")
                    .append("<td class='qty'>").append(formatQuantity(quantity));
                        if (product.isSelected()) {
                            firstPageContent.append("(Order)");
                        }
                    firstPageContent.append("</td>");

                     firstPageContent .append("</tr>");
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
                double quantity = getQuantityFromCell(product);

                firstPageContent.append("<tr>")
                        .append("<td>").append(product.getProductName()).append("</td>")
                        .append("<td class='qty'>").append(formatQuantity(quantity));

                // Add "Kg" for the first product on the second page and "Order" if selected
                if (i == 7 && quantity > 0) {
                    firstPageContent.append(" Kg");
                }
                if (product.isSelected()) {
                    firstPageContent.append("(Order)");
                }

                firstPageContent.append("</td>")
                        .append("</tr>");

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


    }

    /**
     * Handles the "Save and Print Order" action.
     */
    @FXML
    private void saveAndPrintOrder(ActionEvent event) {

        int orderId = saveOrder(event);
        printOrderSummary(orderId);

    }


    @FXML
    private void printOrder(ActionEvent event) {
        printOrderSummary2();
    }

    @FXML
    private void printOrder2(ActionEvent event) {
        printOrderSummary3();
    }

    /**
     * Displays a confirmation message to the user indicating that the order has been successfully saved and printed.
     */
    private void showConfirmationMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Order Saved and Printed");
        alert.showAndWait();
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

    @FXML
    private void clearData() {

        branchComboBox.getSelectionModel().clearSelection();
        for (Map.Entry<Integer, TextField> entry : productQuantityMap.entrySet()) {
            entry.getValue().clear();
        }

        for (TextField quantityField : productQuantityMap.values()) {
            quantityField.clear();
        }
        clearCheckboxes(productTable1);
        clearCheckboxes(productTable2);
        clearCheckboxes(productTable3);

    }

    /**
     * Handles the close button action by prompting the user with a confirmation alert before closing the application.
     */
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

    /**
     * Handles the action when the "Order Summary" button is clicked.
     */
    @FXML
    private void orderSummaryButtonClick() {
        try {
            // Load the TotalSummary.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource
                    ("/com/example/foodordersystem/TotalSummary.fxml"));
            Parent root = loader.load();

            // Create a new stage (window) to show the TotalSummary page
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void summaryGenerate() {
        try {
            // Load the TotalSummary.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource
                    ("/com/example/foodordersystem/BillGenerate.fxml"));
            Parent root = loader.load();

            // Create a new stage (window) to show the TotalSummary page
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void searchOrderButton(ActionEvent actionEvent) {
    }

    public void updateButtonClick(ActionEvent actionEvent) {

        try {
            // Load the TotalSummary.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource
                    ("/com/example/foodordersystem/OrderSummery.fxml"));
            Parent root = loader.load();

            // Create a new stage (window) to show the TotalSummary page
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void loadProductsForOrder(int orderId) {
        // Fetch order products
        List<OrderProduct> orderProducts = orderRepository.getProductsForOrder(orderId);
        for (OrderProduct op : orderProducts) {
            System.out.println("Order ID: " + op.getOrderId() + ", Product ID: " + op.getProductId() + ", Quantity: " + op.getQuantity());
        }

        // Fetch all products
        List<Product> allProducts = productService.getAllProducts();
        System.out.println("All Products Retrieved: " + allProducts.size());

        // Create a map linking product ID to quantity
        Map<Integer, Double> productQuantityMap = orderProducts.stream()
                .collect(Collectors.toMap(OrderProduct::getProductId, OrderProduct::getQuantity));

        System.out.println("Product Quantity Map: " + productQuantityMap);

        // Update product list with quantity from orderProducts
        List<Product> updatedProducts = allProducts.stream()
                .map(product -> {
                    double quantity = productQuantityMap.getOrDefault(product.getId(), 0.0); // Default to 0.0 if not in order
                    product.setQuantity(quantity);
                    System.out.println("Updated Product: ID=" + product.getId() );
                    return product;
                })
                .collect(Collectors.toList());

        // Distribute products into three tables
        ObservableList<Product> productList1 = FXCollections.observableArrayList();
        ObservableList<Product> productList2 = FXCollections.observableArrayList();
        ObservableList<Product> productList3 = FXCollections.observableArrayList();

        int totalProducts = updatedProducts.size();
        int productsPerTable = (int) Math.ceil((double) totalProducts / 3);

        if (totalProducts > 0) {
            productList1.addAll(updatedProducts.subList(0, Math.min(productsPerTable, totalProducts)));
        }
        if (totalProducts > productsPerTable) {
            productList2.addAll(updatedProducts.subList(productsPerTable, Math.min(productsPerTable * 2, totalProducts)));
        }
        if (totalProducts > productsPerTable * 2) {
            productList3.addAll(updatedProducts.subList(productsPerTable * 2, totalProducts));
        }

//        for (Product product : productTable1.getItems()) {
//            TextField quantityField = new TextField();
//            productQuantityMap.put(product.getId(), quantityField);
//
//            // Add event listener to update totalQuantity dynamically
//            quantityField.textProperty().addListener((observable, oldValue,
//                                                      newValue) -> updateTotalQuantity());
//        }

        // Set the product lists to the tables
        productTable1.setItems(productList1);
        productTable2.setItems(productList2);
        productTable3.setItems(productList3);
    }

}