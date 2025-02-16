package com.example.foodordersystem.model;

import javafx.beans.property.*;

public class ProductViewModel {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty quantity;

    public ProductViewModel(Product product, double quantity) {
        this.id = new SimpleIntegerProperty(product.getId());
        this.name = new SimpleStringProperty(product.getProductName());
        this.quantity = new SimpleDoubleProperty(quantity);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public double getQuantity() {
        return quantity.get();
    }

    public void setQuantity(double quantity) {
        this.quantity.set(quantity);
    }

    public DoubleProperty quantityProperty() {
        return quantity;
    }
}
