package com.example.foodordersystem.controller;

import com.example.foodordersystem.model.Product;
import com.example.foodordersystem.service.ProductService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AddProductController {

    @FXML
    private TextField productNameField;

    @FXML
    private TextField productCodeField;

    @FXML
    private Button saveButton;

    private final ProductService productService = new ProductService();

    @FXML
    private void handleSaveProduct() {
        String productName = productNameField.getText();
        String productCode = productCodeField.getText();

        Product product = new Product();
        product.setProductName(productName);
        product.setProductCode(productCode);

        boolean isSaved = productService.saveProduct(product);
        if (isSaved) {
            showAlert("Success", "Product saved successfully!", Alert.AlertType.INFORMATION);
            clearFields();
        } else {
            showAlert("Error", "Failed to save product. Check the input fields.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        productNameField.clear();
        productCodeField.clear();
    }

}
