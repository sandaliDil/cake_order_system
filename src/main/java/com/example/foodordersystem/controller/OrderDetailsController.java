package com.example.foodordersystem.controller;

import com.example.foodordersystem.model.Order;
import com.example.foodordersystem.model.OrderProduct;
import com.example.foodordersystem.model.Product;
import com.example.foodordersystem.repository.OrderProductRepository;
import com.example.foodordersystem.repository.OrderRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class OrderDetailsController {

    @FXML
    private Label orderIdLabel;
    @FXML
    private Label orderDateLabel;
    @FXML
    private Label orderOptionLabel;
    @FXML
    private TableView<OrderProduct> orderDetailsTable;
    @FXML
    private TableColumn<OrderProduct, String> productNameColumn;
    @FXML
    private TableColumn<OrderProduct, Integer> quantityColumn;
    @FXML
    private TableColumn<OrderProduct, Double> priceColumn;

    private final OrderRepository orderRepository = new OrderRepository();
    private final OrderProductRepository orderProductRepository = new OrderProductRepository();

    public void loadOrderDetails(int orderId) {
        Order order = orderRepository.getOrderById(orderId);
        if (order != null) {
            orderIdLabel.setText(String.valueOf(order.getId()));
            orderDateLabel.setText(order.getOrderDate().toString());
            orderOptionLabel.setText(order.getOption());

            List<OrderProduct> orderProducts = orderProductRepository.getOrderDetails(orderId);
            ObservableList<OrderProduct> orderProductList = FXCollections.observableArrayList(orderProducts);
            orderDetailsTable.setItems(orderProductList);
        }
    }

    @FXML
    public void initialize() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
    }
    @FXML
    private void printOrder() {
        System.out.println("Printing order...");
    }

    @FXML
    private void saveAndPrintOrder() {
        System.out.println("Order saved and printed...");
    }

}
