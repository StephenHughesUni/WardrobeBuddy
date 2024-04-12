package com.example.wardrobebuddy.com.firebaseauth;

public class ScannedItem {
    private String imageUri;
    private String size;
    private String price;

    private String brand; // Field to store the brand name
    private String articleNumber;

    private String dateTimeScanned; // Field to store the scan date/time

    private String category; // New field for category

    // No-argument constructor required for Firebase
    public ScannedItem() {
    }

    // Constructor
    public ScannedItem(String imageUri, String brand, String size, String price, String articleNumber, String dateTimeScanned, String category) {
        this.imageUri = imageUri;
        this.brand = brand;
        this.size = size;
        this.price = price;
        this.articleNumber = articleNumber;
        this.dateTimeScanned = dateTimeScanned;
        this.category = category;

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

    // Getter and setter for the brand
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
}