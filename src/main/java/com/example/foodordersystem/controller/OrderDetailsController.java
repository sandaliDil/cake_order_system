package com.example.foodordersystem.controller;

import com.example.foodordersystem.model.OrderProduct;
import com.example.foodordersystem.repository.OrderProductRepository;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class OrderDetailsController {

    public Label orderIdLabel;
    @FXML
    private TableView<OrderProduct> orderDetailsTable;
    @FXML
    private TableColumn<OrderProduct, String> productNameColumn;
    @FXML
    private TableColumn<OrderProduct, Double> quantityColumn;

    private final OrderProductRepository orderProductRepository = new OrderProductRepository();
    private int orderId;

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        loadOrderDetails();
    }

    @FXML
    public void initialize() {
        if (orderDetailsTable == null) {
            System.out.println("orderDetailsTable is NULL in initialize!");
        } else {
            productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        }
    }


    private void loadOrderDetails() {
        ObservableList<OrderProduct> orderDetails = orderProductRepository.getOrderDetails(orderId);
        orderDetailsTable.setItems(orderDetails);
    }
}
