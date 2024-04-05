package com.example.wardrobebuddy.com.firebaseauth;

public class ProductInfoResponse {
    private ProductInfo product_info; // Matches "product_info" in JSON

    // Constructor, getters, and setters
    public ProductInfo getProductInfo() {
        return product_info;
    }

    public void setProductInfo(ProductInfo product_info) {
        this.product_info = product_info;
    }
}
