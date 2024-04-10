package com.example.wardrobebuddy.com.firebaseauth;

import java.util.List;
import java.util.Map;

public class CollectionItem {
    private String brand;
    private String dateTimeScanned;
    private String size;
    private String price;
    private String articleNumber;
    private String imageUri; // URL to the image of the scanned item
    private Map<String, Boolean> sizeAvailability;
    private List<LocationInfo> locationInfo;
    private String productImageUrl; // URL of the product image from ProductInfo

    // Constructor
    public CollectionItem(ScannedItem scannedItem, ProductInfo productInfo) {
        this.brand = scannedItem.getBrand();
        this.dateTimeScanned = scannedItem.getDateTimeScanned();
        this.size = scannedItem.getSize();
        this.price = scannedItem.getPrice();
        this.articleNumber = scannedItem.getArticleNumber();
        this.imageUri = scannedItem.getImageUri();
        this.sizeAvailability = productInfo.getSizeAvailability();
        this.locationInfo = productInfo.getLocationInfo();
        this.productImageUrl = productInfo.getImageUrl();
    }

    // Getters
    public String getBrand() { return brand; }
    public String getDateTimeScanned() { return dateTimeScanned; }
    public String getSize() { return size; }
    public String getPrice() { return price; }
    public String getArticleNumber() { return articleNumber; }
    public String getImageUri() { return imageUri; }
    public Map<String, Boolean> getSizeAvailability() { return sizeAvailability; }
    public List<LocationInfo> getLocationInfo() { return locationInfo; }
    public String getProductImageUrl() { return productImageUrl; }

    // Setters
    public void setBrand(String brand) { this.brand = brand; }
    public void setDateTimeScanned(String dateTimeScanned) { this.dateTimeScanned = dateTimeScanned; }
    public void setSize(String size) { this.size = size; }
    public void setPrice(String price) { this.price = price; }
    public void setArticleNumber(String articleNumber) { this.articleNumber = articleNumber; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public void setSizeAvailability(Map<String, Boolean> sizeAvailability) { this.sizeAvailability = sizeAvailability; }
    public void setLocationInfo(List<LocationInfo> locationInfo) { this.locationInfo = locationInfo; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
}
