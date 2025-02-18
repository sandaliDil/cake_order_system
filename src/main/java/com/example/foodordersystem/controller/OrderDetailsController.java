package com.example.foodordersystem.controller;

import com.example.foodordersystem.Session;
import com.example.foodordersystem.model.*;
import com.example.foodordersystem.repository.OrderRepository;
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

import javax.swing.*;
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
    @FXML private TableColumn<Product, Double> quantityColumn;



    private final OrderRepository orderRepository = new OrderRepository();
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
//    private void loadProducts() {
//        List<Product> allProducts = productService.getAllProducts();
//
//        ObservableList<Product> productList1 = FXCollections.observableArrayList();
//        ObservableList<Product> productList2 = FXCollections.observableArrayList();
//        ObservableList<Product> productList3 = FXCollections.observableArrayList();
//
//        int totalProducts = allProducts.size();
//        int productsPerTable = 10;
//
//        // Distribute the products across the three tables
//        if (totalProducts > 0) {
//            productList1.addAll(allProducts.subList(0, Math.min(productsPerTable, totalProducts)));
//        }
//        if (totalProducts > productsPerTable) {
//            productList2.addAll(allProducts.subList(productsPerTable, Math.min(productsPerTable * 2, totalProducts)));
//        }
//        if (totalProducts > productsPerTable * 2) {
//            productList3.addAll(allProducts.subList(productsPerTable * 2, Math.min(productsPerTable * 3,
//                    totalProducts)));
//        }
//
//        // Set the items for each table
//        productTable1.setItems(productList1);
//        productTable2.setItems(productList2);
//        productTable3.setItems(productList3);
//
//        // Create quantity columns
//        TableColumn<Product, String> quantityColumn1 = createQuantityColumn();
//        TableColumn<Product, String> quantityColumn2 = createQuantityColumn();
//        TableColumn<Product, String> quantityColumn3 = createQuantityColumn();
//
//        // Create checkbox columns
//        TableColumn<Product, Boolean> checkboxColumn1 = createCheckboxColumn();
//        TableColumn<Product, Boolean> checkboxColumn2 = createCheckboxColumn();
//        TableColumn<Product, Boolean> checkboxColumn3 = createCheckboxColumn();
//
//        // Add the columns to the tables
//        productTable1.getColumns().addAll(checkboxColumn1, quantityColumn1);
//        productTable2.getColumns().addAll(checkboxColumn2, quantityColumn2);
//        productTable3.getColumns().addAll(checkboxColumn3, quantityColumn3);
//    }

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

    private Order getSelectedOrder() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private boolean updateOrder(ActionEvent event) {
        String username = usernameLabel.getText();
        System.out.println("Username: " + username);

        User loggedInUser = Session.getInstance().getLoggedInUser();
        if (loggedInUser == null) {
            System.out.println("No user is logged in.");
            showAlert("Error", "Please log in to update the order.");
            return false;
        }

        Order order = getSelectedOrder(); // Assume this method retrieves the selected order

        if (order == null) {
            showAlert("Error", "No order selected for update.");
            return false;
        }

        // Get Order ID
        int orderId = order.getId();
        System.out.println("Updating Order ID: " + orderId);

        order.setUserId(loggedInUser.getId());

        // Validate branch selection
        String selectedBranchName = branchComboBox.getValue();
        if (selectedBranchName == null) {
            System.out.println("Branch is not selected.");
            showAlert("Error", "Please select a branch.");
            return false;
        }

        Branch selectedBranch = getBranchByName(selectedBranchName);
        if (selectedBranch == null) {
            System.out.println("Branch not found.");
            showAlert("Error", "Selected branch is not valid.");
            return false;
        }

        order.setBranchId(selectedBranch.getId());

        // Validate order date
        if (orderDatePicker.getValue() == null) {
            System.out.println("Order date is not selected.");
            showAlert("Error", "Please select an order date.");
            return false;
        }
        order.setOrderDate(orderDatePicker.getValue().atStartOfDay());

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

        // Validate and update order products
        List<OrderProduct> orderProducts = new ArrayList<>();
        if (!addOrderProductsFromTable(productTable1, orderProducts) ||
                !addOrderProductsFromTable(productTable2, orderProducts) ||
                !addOrderProductsFromTable(productTable3, orderProducts)) {
            System.out.println("Invalid quantity in order.");
            showAlert("Error", "Please enter valid quantities for all products.");
            return false;
        }

        order.setItems(orderProducts);
        order.setStatus(true);

        // Call the method to load order details (Optional)
        loadOrderDetails(order);

        // Update the order
        boolean isUpdated = orderService.updateOrder(order);

        if (isUpdated) {
            System.out.println("Order updated successfully.");
            showAlert("Success", "Order updated successfully.");
            return true;
        } else {
            showAlert("Error", "Failed to update the order. Please try again.");
            return false;
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
    private void printOrderSummary(boolean orderId) {
        StringBuilder firstPageContent = new StringBuilder();
        StringBuilder secondPageContent = new StringBuilder();

        // Get user details
        String OrderId = orderIdTextField.getText();
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
                .append("<div><strong>Date:</strong> ").append(orderDate).append("</div>")
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
                    .append("<td>").append(product.getProductName()).append("</td>");
            firstPageContent.append("<td class='qty'>").append(formatQuantity(getQuantityFromCell(product)));
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
                        .append("<td>").append(product.getProductName()).append("</td>");
                firstPageContent.append("<td class='qty'>").append(formatQuantity(getQuantityFromCell(product)));

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
                    secondPageContent.append("<td class='qty'>").append(formatQuantity(getQuantityFromCell(product)));
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
        String username = usernameLabel.getText();
        String orderDate = orderDatePicker.getValue().toString();
        String selectedBranchName = branchComboBox.getValue();

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

    @FXML
    private void printOrderSummary3() {
        StringBuilder firstPageContent = new StringBuilder();

        // Get user details
        String username = usernameLabel.getText();
        String orderDate = orderDatePicker.getValue().toString();
        String selectedBranchName = branchComboBox.getValue();

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
                .append("<div><strong>Date:</strong> ").append(orderDate).append("</div>")
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
    private void saveAndPrintOrder(ActionEvent event) {
        boolean orderId = updateOrder(event);
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

    public void loadOrderDetails(Order order) {
        usernameLabel.setText(order.getUserName() +" online"); // Assuming `getUsername()` exists in Order
        System.out.println(order.getUserName());
        orderDatePicker.setValue(order.getOrderDate()); // Assuming it's a LocalDate
        System.out.println(order.getOrderDate());
         loadProductsForOrder(order.getId()); // Load product details

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
}