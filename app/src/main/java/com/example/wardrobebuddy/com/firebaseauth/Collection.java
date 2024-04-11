package com.example.wardrobebuddy.com.firebaseauth;

import java.util.ArrayList;
import java.util.List;

public class Collection {
    private String name;
    private List<CollectionItem> items;

    public Collection() {
        // Firebase needs the no-arg constructor
    }

    public String getFirstItemImageUrl() {
        if(items != null && !items.isEmpty() && items.get(0).getProductImageUrl() != null) {
            return items.get(0).getProductImageUrl();
        }
        return null; // return a default image or null
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public Collection(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CollectionItem> getItems() {
        return items;
    }

    public void setItems(List<CollectionItem> items) {
        this.items = items;
    }

    public void addItem(CollectionItem item) {
        if(this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
}

