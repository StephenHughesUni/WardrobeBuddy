package com.example.wardrobebuddy.com.firebaseauth;

import java.util.Map;
import java.util.List;

public class ProductInfo {
    private Map<String, Boolean> size_availability; // Use directly the Map for simplicity
    private List<LocationInfo> location_info; // A list of location info objects
    private String image_url; // URL of the product image

    // Constructor, getters, and setters
    public Map<String, Boolean> getSizeAvailability() {
        return size_availability;
    }

    public void setSizeAvailability(Map<String, Boolean> size_availability) {
        this.size_availability = size_availability;
    }

    public List<LocationInfo> getLocationInfo() {
        return location_info;
    }

    public void setLocationInfo(List<LocationInfo> location_info) {
        this.location_info = location_info;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }
}
