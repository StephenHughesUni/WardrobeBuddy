package com.example.wardrobebuddy.com.firebaseauth;

import android.net.Uri;

public class ScannedItem {
    private Uri imageUri;
    private String recognizedText; // You might want to change this to more specific fields
    private String size;
    private String price;
    private String articleNumber;

    // Constructor, getters, and setters
    public ScannedItem(Uri imageUri, String size, String price, String articleNumber) {
        this.imageUri = imageUri;
        this.size = size;
        this.price = price;
        this.articleNumber = articleNumber;
    }

    // Getters (and optionally setters)
    public Uri getImageUri() { return imageUri; }
    public String getSize() { return size; }
    public String getPrice() { return price; }
    public String getArticleNumber() { return articleNumber; }
}
