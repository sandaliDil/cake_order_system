package com.example.foodordersystem.controller;

import com.example.foodordersystem.model.Branch;
import com.example.foodordersystem.service.BranchService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class BranchController {

    @FXML
    private TextField branchIdField;
    @FXML
    private TextField searchBox;
    @FXML
    private Label branchCountLabel1;
    @FXML
    private TextField branchNameField;
    @FXML
    private TextField branchCodeField;
    @FXML
    private TableView<Branch> branchTable;
    @FXML
    private TableColumn<Branch, String> branchNameColumn;
    @FXML
    private TableColumn<Branch, String> branchCodeColumn;
    @FXML
    private TableColumn<Branch, Void> actionColumn;

    private final BranchService branchService = new BranchService();

    private ObservableList<Branch> branchList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind table columns to Branch properties
        branchNameColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        branchCodeColumn.setCellValueFactory(new PropertyValueFactory<>("branchCode"));

        // Load data into the table and add action buttons
        loadBranchData();
        addActionButtons();
        updateBranchCount();
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> loadBranches(newValue));

    }

    private void updateBranchCount() {
        int branchCount = branchService.getBranchCount();
        branchCountLabel1.setText("0" + branchCount);
    }

    private void loadBranches(String searchQuery) {
        List<Branch> branches = branchService.searchBranchesByName(searchQuery);
        ObservableList<Branch> branchObservableList = FXCollections.observableArrayList(branches);
        branchTable.setItems(branchObservableList);
    }

    private void loadBranchData() {
        branchList.clear();
        branchList.addAll(branchService.getAllBranches());
        branchTable.setItems(branchList);
    }

    private void addActionButtons() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createButton("Edit", "#4CAF50");
            private final Button deleteButton = createButton("Delete", "#f44336");
            private final HBox actionBox = new HBox(editButton, deleteButton);

            {
                actionBox.setSpacing(10); // Add spacing between buttons
                actionBox.setAlignment(Pos.CENTER); // Center the buttons within the HBox

                // Edit Button Action
                editButton.setOnAction(event -> onEditButtonClicked(getTableView().getItems().get(getIndex())));

                // Delete Button Action
                deleteButton.setOnAction(event -> onDeleteButtonClicked(getTableView().getItems().get(getIndex())));
            }



            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        return button;
    }
    @FXML
    private void onEditButtonClicked(Branch selectedBranch) {
        if (selectedBranch != null) {
            // Create a new dialog for editing branch details
            Dialog<Branch> dialog = new Dialog<>();
            dialog.setTitle("Edit Branch");
            dialog.setHeaderText("Edit the branch details below:");

            // Set dialog button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create text fields for editing branch details
            TextField branchNameInput = new TextField(selectedBranch.getBranchName());
            TextField branchCodeInput = new TextField(selectedBranch.getBranchCode());

            // Name the text fields using labels
            Label nameLabel = new Label("Branch Name:");
            Label codeLabel = new Label("Branch Code:");

            // Layout for the dialog content
            GridPane gridPane = new GridPane();
            gridPane.setHgap(10); // Horizontal gap between columns
            gridPane.setVgap(10); // Vertical gap between rows
            gridPane.add(nameLabel, 0, 0); // Add label at column 0, row 0
            gridPane.add(branchNameInput, 1, 0); // Add text field at column 1, row 0
            gridPane.add(codeLabel, 0, 1); // Add label at column 0, row 1
            gridPane.add(branchCodeInput, 1, 1); // Add text field at column 1, row 1

            dialog.getDialogPane().setContent(gridPane);

            // Convert the result when the Save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Return a new Branch object with updated details
                    return new Branch(selectedBranch.getId(), branchNameInput.getText(), branchCodeInput.getText());
                }
                return null; // Return null if the dialog is cancelled
            });

            // Show the dialog and wait for user input
            Optional<Branch> result = dialog.showAndWait();

            result.ifPresent(updatedBranch -> {
                // Update the branch in the database
                if (branchService.updateBranch(updatedBranch)) {
                    showAlert(Alert.AlertType.INFORMATION, "Branch Updated", "Branch details updated successfully.");
                    loadBranchData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update the branch. Please try again.");
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a branch to edit.");
        }
    }

    @FXML
    private void onDeleteButtonClicked(Branch selectedBranch) {
        if (selectedBranch != null) {
            if (branchService.deleteBranch(selectedBranch.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Branch Deleted", "Branch deleted successfully.");
                loadBranchData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Failed to delete the branch. Please try again.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a branch to delete.");
        }
    }

    @FXML
    public void addBranch() {
        String name = branchNameField.getText();
        String code = branchCodeField.getText();

        if (name.isEmpty() || code.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Branch name or code cannot be empty!");
            return;
        }

        Branch branch = new Branch(name, code);
        if (branchService.addBranch(branch)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Branch added successfully!");
            clearFields();
            loadBranchData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add branch. Please try again.");
        }
    }

    private void clearFields() {
        branchNameField.clear();
        branchCodeField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
