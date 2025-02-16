package com.example.foodordersystem.controller;

import com.example.foodordersystem.model.Order;
import com.example.foodordersystem.repository.OrderProductRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class OnlineOrderController {

    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Integer> idColumn;
    @FXML
    private TableColumn<Order, String> optionColumn;
    @FXML
    private TableColumn<Order, String> dateColumn;
    @FXML
    private TableColumn<Order, Void> actionColumn;

    private final OrderProductRepository orderService = new OrderProductRepository();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        optionColumn.setCellValueFactory(new PropertyValueFactory<>("option"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        addButtonToTable();
        loadPendingOrders();
    }

    private void loadPendingOrders() {
        List<Order> pendingOrders = orderService.getPendingOrders();
        ObservableList<Order> orderList = FXCollections.observableArrayList(pendingOrders);
        orderTable.setItems(orderList);
    }

    private void addButtonToTable() {
        actionColumn.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button detailsButton = new Button("View Details");

            {
                detailsButton.setOnAction(event -> {
                    Order selectedOrder = getTableView().getItems().get(getIndex());
                    openOrderDetailsPage(selectedOrder, detailsButton); // Pass the button to fix error
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : detailsButton);
            }
        });
    }

    private void openOrderDetailsPage(Order order, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/foodordersystem/OrderDetails.fxml"));
            Parent root = loader.load();

            OrderDetailsController controller = loader.getController();
            controller.loadOrderDetails(order);

            Stage stage = (Stage) sourceButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }


}
