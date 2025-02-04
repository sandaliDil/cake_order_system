package com.example.foodordersystem.controller;

import com.example.foodordersystem.model.Product;
import com.example.foodordersystem.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.List;

public class ProductViewController {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Integer> colProductId;

    @FXML
    private TableColumn<Product, String> colProductCode;

    @FXML
    private TableColumn<Product, String> colProductName;

    @FXML
    private TableColumn<Product, Void> colEdit;

    @FXML
    private TextField productNameField;

    @FXML
    private TextField productCodeField;

    @FXML
    private Label productCountLabel;

    private final ProductService productService;
    private List<Product> products;

    public ProductViewController() {
        this.productService = new ProductService();
    }

    @FXML
    public void initialize() {
        // Setup table columns
        colProductId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));

        // Load products from the database
        products = productService.getAllProducts();
        if (products != null) {
            loadProductTable();
            updateProductCountLabel(); // Update count label
        }

        // Add edit and delete buttons to the table
        addEditButtonsToTable();
    }

    @FXML
    private void handleSaveProduct() {
        String productName = productNameField.getText();
        String productCode = productCodeField.getText();

        // Create a new product instance
        Product product = new Product();
        product.setProductName(productName);
        product.setProductCode(productCode);

        // Save the product using the service layer
        boolean isSaved = productService.saveProduct(product);
        if (isSaved) {
            showAlert("Success", "Product saved successfully!", Alert.AlertType.INFORMATION);
            products = productService.getAllProducts(); // Refresh the list
            loadProductTable();
            updateProductCountLabel(); // Update count after adding
            clearFields();
        } else {
            showAlert("Error", "Failed to save product. Check the input fields.", Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        productNameField.clear();
        productCodeField.clear();
    }

    private void updateProductCountLabel() {
        productCountLabel.setText(String.valueOf(products.size()));
    }

    private void loadProductTable() {
        ObservableList<Product> productObservableList = FXCollections.observableArrayList(products);
        productTable.setItems(productObservableList);
    }

    private void addEditButtonsToTable() {
        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                // Style buttons
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                // Add hover effect
                editButton.setCursor(javafx.scene.Cursor.HAND);
                deleteButton.setCursor(javafx.scene.Cursor.HAND);

                // Set button actions
                editButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showEditDialog(product);
                });

                deleteButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDeleteAction(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttonContainer = new HBox(10, editButton, deleteButton);
                    buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(buttonContainer);
                }
            }
        };

        colEdit.setCellFactory(cellFactory);
    }

    private void handleDeleteAction(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setContentText("Are you sure you want to delete this product?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (productService.deleteProduct(product.getId())) {
                    showAlert("Success", "Product deleted successfully!", Alert.AlertType.INFORMATION);
                    products = productService.getAllProducts(); // Refresh the list
                    loadProductTable();
                    updateProductCountLabel(); // Update count after deletion
                } else {
                    showAlert("Error", "Failed to delete product.", Alert.AlertType.ERROR);
                }
            }
        });
    }
    private void showEditDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Edit Product Details");

        // TextFields for editing product details
        TextField productIdField = new TextField(String.valueOf(product.getId()));
        productIdField.setDisable(true); // Disable the ID field, since it shouldn't be editable

        TextField productCodeField = new TextField(product.getProductCode());
        TextField productNameField = new TextField(product.getProductName());

        // Create a layout for the dialog
        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Product Code:"), productCodeField,
                new Label("Product Name:"), productNameField
        );
        dialog.getDialogPane().setContent(content);

        // Add buttons to the dialog
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Handle save action
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                product.setProductName(productNameField.getText());
                product.setProductCode(productCodeField.getText());
                return product; // Return the updated product
            }
            return null;
        });

        // Show the dialog and handle the result
        dialog.showAndWait().ifPresent(updatedProduct -> {
            if (updatedProduct != null) {
                // Update the product in the service layer
                boolean isUpdated = productService.updateProduct(updatedProduct);
                if (isUpdated) {
                    showAlert("Success", "Product updated successfully!", Alert.AlertType.INFORMATION);
                    products = productService.getAllProducts(); // Refresh the list of products
                    loadProductTable(); // Reload the table with updated data
                    updateProductCountLabel(); // Update the count label
                } else {
                    showAlert("Error", "Failed to update product.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
