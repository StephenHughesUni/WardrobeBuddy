package com.example.wardrobebuddy.com.firebaseauth;

public class UrlBuilder {

    public static String getUrl(String brand, String articleNumber) {
        String encodedArticleNumber = articleNumber.replace("/", "%2F");

        switch (brand.toLowerCase()) {
            case "zara":
                return "https://www.zara.com/ie/en/search?searchTerm=" + encodedArticleNumber + "&section=WOMAN";
            // Add more cases for different brands here
            default:
                // Fallback URL (could be a search engine)
                return "https://www.google.com/search?q=" + brand + "+" + encodedArticleNumber;
        }
    }
}
