package com.example.wardrobebuddy.com.firebaseauth;

public class CollectionItem {
    private String imageUri; // Change this field to String
    private String size;
    private String price;
    private boolean isSelected; // Default should be false
    private String brand; // Field to store the brand name
    private String articleNumber;

    private String dateTimeScanned; // Field to store the scan date/time

    private String category; // New field for category
    private String productImageUrl; // New field for product image URL

    // No-argument constructor required for Firebase
    public CollectionItem() {
    }

    // Constructor
    public CollectionItem(String imageUri, String brand, String size, String price, String articleNumber, String dateTimeScanned, String category, String productImageUrl) {
        this.imageUri = imageUri;
        this.brand = brand;
        this.size = size;
        this.price = price;
        this.articleNumber = articleNumber;
        this.dateTimeScanned = dateTimeScanned;
        this.category = category;
        this.productImageUrl = productImageUrl;
    }

    // Getters and setters
    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getDateTimeScanned() {
        return dateTimeScanned;
    }

    public void setDateTimeScanned(String dateTimeScanned) {
        this.dateTimeScanned = dateTimeScanned;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
