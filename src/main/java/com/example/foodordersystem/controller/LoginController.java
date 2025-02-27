package com.example.foodordersystem.controller;

import com.example.foodordersystem.Session;
import com.example.foodordersystem.model.User;
import com.example.foodordersystem.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String userName = usernameField.getText();
        String password = passwordField.getText();

        if ("123".equals(userName) && "111".equals(password)) {
            Session.getInstance().setLoggedInUser(new User(userName)); // Simulating user creation
            showAlert("Login Successful", "Welcome, " + userName + "!", Alert.AlertType.INFORMATION);
            loadDashboard();
        } else {
            User loggedInUser = authenticateUser(userName, password);
            if (loggedInUser != null) {
                Session.getInstance().setLoggedInUser(loggedInUser);
                showAlert("Login Successful", "Welcome, " + loggedInUser.getUserName() + "!", Alert.AlertType.INFORMATION);
                loadOrder();
            } else {
                showAlert("Login Failed", "Invalid username or password.", Alert.AlertType.ERROR);
            }
        }
    }

    private User authenticateUser(String username, String password) {
        return userService.findUserByUsernameAndPassword(username, password);
    }

    @FXML
    public void initialize() {
        usernameField.setOnKeyPressed(this::handleUsernameFieldEnter);
        passwordField.setOnKeyPressed(this::handlePasswordFieldEnter);
    }

    private void handleUsernameFieldEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            passwordField.requestFocus();
        }
    }

    private void handlePasswordFieldEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    @FXML
    private void handleExitButton(ActionEvent event) {
        System.exit(0);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadOrder() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/foodordersystem/Orders.fxml"));
            Parent root = fxmlLoader.load();
            OrderController orderController = fxmlLoader.getController();
            orderController.setUsername(usernameField.getText());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Order Page.", Alert.AlertType.ERROR);
        }
    }

    // Method to load the Dashboard page
    private void loadDashboard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/foodordersystem/Dashboard.fxml"));
            Parent root = fxmlLoader.load();


            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Dashboard.", Alert.AlertType.ERROR);
        }
    }
}
