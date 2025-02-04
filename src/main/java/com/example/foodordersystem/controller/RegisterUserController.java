package com.example.foodordersystem.controller;

import com.example.foodordersystem.model.User;
import com.example.foodordersystem.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class RegisterUserController {

    @FXML
    private TextField userNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> userNameColumn;

    @FXML
    private TableColumn<User, String> passwordColumn;

    @FXML
    private TableColumn<User, String> createdAtColumn;

    @FXML
    private Label productCountLabel;

    @FXML
    private TableColumn<User, String> actionColumn;  // New column for Edit/Delete buttons

    private final UserService userService = new UserService();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    /**
     * Initializes the controller. Sets up the table columns and loads existing users.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        loadUsers();
        int userCount = userList.size();
        productCountLabel.setText(String.valueOf("0" +userCount));
    }

    /**
     * Sets up the table columns with the corresponding properties from the User model.
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));

        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().toString());
            }
            return new SimpleStringProperty("N/A");
        });

        // Set up the action column (Edit and Delete buttons)
        actionColumn.setCellFactory(col -> {
            TableCell<User, String> cell = new TableCell<>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox buttonBar = new HBox(10, editButton, deleteButton);
                        setGraphic(buttonBar);

                        // Edit button action
                        editButton.setOnAction(event -> handleEdit(getTableRow().getItem()));

                        // Delete button action
                        deleteButton.setOnAction(event -> handleDelete(getTableRow().getItem()));
                    }
                }
            };
            return cell;
        });
    }

    /**
     * Loads the users from the database and populates the table.
     */
    private void loadUsers() {
        userList.clear();
        userList.addAll(userService.getAllUsers());
        userTable.setItems(userList);
    }

    /**
     * Handles saving a new user.
     */
    @FXML
    public void saveUser(ActionEvent event) {
        String username = userNameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Please enter all fields");
            return;
        }

        boolean success = userService.saveUser(username, password);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "User saved successfully!");
            loadUsers();  // Refresh the table

            // Clear text fields after saving the user
            userNameField.clear();
            passwordField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "There was an error saving the user.");
        }
    }

    /**
     * Handles editing a user.
     */
    private void handleEdit(User user) {
        // Create a dialog for editing the user
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");

        // Create the dialog content
        Label userNameLabel = new Label("Username:");
        TextField userNameField = new TextField(user.getUserName());

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setText(user.getPassword());

        // Add content to the dialog
        dialog.getDialogPane().setContent(new VBox(10, userNameLabel, userNameField, passwordLabel, passwordField));

        // Add "Save" button to dialog
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Set action on Save button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setUserName(userNameField.getText());
                user.setPassword(passwordField.getText());
                return user;
            }
            return null;
        });

        // Show the dialog and handle the result
        dialog.showAndWait().ifPresent(updatedUser -> {
            boolean updated = userService.updateUser(updatedUser);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
                loadUsers();  // Refresh the table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user.");
            }
        });
    }

    /**
     * Handles deleting a user.
     */
    private void handleDelete(User user) {
        boolean deleted = userService.deleteUser(user.getId());
        if (deleted) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
            loadUsers();  // Refresh the table
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
        }
    }

    /**
     * Displays an alert to the user.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
