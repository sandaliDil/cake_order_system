package com.example.foodordersystem.model;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Product extends Order {

    private int id;
    private String productCode;
    private String productName;

    public Product(int id, String productName) {
        this.id = id;
        this.productName = productName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Product(String productName, int id, String productCode) {
        this.productName = productName;
        this.id = id;
        this.productCode = productCode;
    }

    public Product() {
    }

    private BooleanProperty selected = new SimpleBooleanProperty(false); // Initialize with false by default

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }


    public void setQuantity(Double aDouble) {
    }
}
