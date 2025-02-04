package com.example.foodordersystem.service;

import com.example.foodordersystem.model.Product;
import com.example.foodordersystem.repository.ProductRepository;

import java.util.List;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
    }

    public List<Product> getAllProducts() {
        return productRepository.getAllProducts();
    }

    public Product getProductById(int productId) {
        return productRepository.findById(productId);
    }

    public boolean saveProduct(Product product) {
        if (isValidProduct(product)) {
            return productRepository.saveProduct(product);
        }
        System.out.println("Product validation failed. Cannot save product.");
        return false;
    }

    public boolean updateProduct(Product product) {
        if (isValidProduct(product) && product.getId() > 0) {
            return productRepository.updateProduct(product);
        }
        System.out.println("Product validation failed or product ID is invalid. Cannot update product.");
        return false;
    }

    public boolean deleteProduct(int productId) {
        if (productId > 0) {
            return productRepository.deleteProduct(productId);
        }
        System.out.println("Invalid product ID. Cannot delete product.");
        return false;
    }

    private boolean isValidProduct(Product product) {
        return product.getProductName() != null && !product.getProductName().isEmpty()
                && product.getProductCode() != null && !product.getProductCode().isEmpty();
    }

    public int getProductCount() {
        return productRepository.getProductCount();
    }

}
