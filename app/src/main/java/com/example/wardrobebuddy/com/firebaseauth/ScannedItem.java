package com.example.wardrobebuddy.com.firebaseauth;

public class ScannedItem {
    private String imageUri; // Change this field to String
    private String size;
    private String price;
    private String articleNumber;

    private String dateTimeScanned; // Field to store the scan date/time

    // No-argument constructor required for Firebase
    public ScannedItem() {
    }

    // Constructor
    public ScannedItem(String imageUri, String size, String price, String articleNumber, String dateTimeScanned) {
        this.imageUri = imageUri;
        this.size = size;
        this.price = price;
        this.articleNumber = articleNumber;
        this.dateTimeScanned = dateTimeScanned;
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
}